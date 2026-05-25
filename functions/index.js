/**
 * SSLCommerz sandbox integration for HandymanApp
 *
 * Uses Firebase Functions v2 params/secrets (replaces deprecated functions.config()).
 *
 * Set secrets with:
 *   firebase functions:secrets:set SSLCOMMERZ_STORE_ID
 *   firebase functions:secrets:set SSLCOMMERZ_STORE_PASSWD
 * (You'll be prompted to paste the value for each.)
 */

const {onRequest} = require("firebase-functions/v2/https");
const {defineSecret, defineString} = require("firebase-functions/params");
const {logger} = require("firebase-functions/v2");
const admin = require("firebase-admin");
const axios = require("axios");
const qs = require("querystring");

admin.initializeApp();
const db = admin.database();

// Secrets - set via `firebase functions:secrets:set NAME`
const SSLCZ_STORE_ID = defineSecret("SSLCOMMERZ_STORE_ID");
const SSLCZ_STORE_PASSWD = defineSecret("SSLCOMMERZ_STORE_PASSWD");

// Non-secret config - defaults to sandbox; override with environment variable
const SSLCZ_BASE_URL = defineString("SSLCOMMERZ_BASE", {
  default: "https://sandbox.sslcommerz.com",
});

/**
 * POST /initPayment
 * Body: { jobId, customerId, amount, customerName?, customerEmail?, customerPhone? }
 * Returns: { GatewayPageURL, tranId }
 */
exports.initPayment = onRequest(
    {secrets: [SSLCZ_STORE_ID, SSLCZ_STORE_PASSWD], cors: true},
    async (req, res) => {
      if (req.method !== "POST") {
        return res.status(405).send({error: "POST required"});
      }

      const {jobId, customerId, amount} = req.body || {};
      if (!jobId || !customerId || !amount) {
        return res.status(400).send({error: "jobId, customerId, amount required"});
      }

      const tranId = `HM_${jobId}_${Date.now()}`;

      const projectId = process.env.GCLOUD_PROJECT;
      const region = "us-central1";
      const base = `https://${region}-${projectId}.cloudfunctions.net`;

      const payload = {
        store_id: SSLCZ_STORE_ID.value(),
        store_passwd: SSLCZ_STORE_PASSWD.value(),
        total_amount: Number(amount).toFixed(2),
        currency: "BDT",
        tran_id: tranId,
        success_url: `${base}/sslRedirect?status=success&tran_id=${tranId}`,
        fail_url: `${base}/sslRedirect?status=fail&tran_id=${tranId}`,
        cancel_url: `${base}/sslRedirect?status=cancel&tran_id=${tranId}`,
        ipn_url: `${base}/sslIpn`,
        multi_card_name: "bkash",
        cus_name: req.body.customerName || "Handyman Customer",
        cus_email: req.body.customerEmail || "customer@handyman.app",
        cus_phone: req.body.customerPhone || "01700000000",
        cus_add1: "Dhaka",
        cus_city: "Dhaka",
        cus_country: "Bangladesh",
        shipping_method: "NO",
        product_name: "Handyman service payment",
        product_category: "Service",
        product_profile: "general",
        value_a: jobId,
        value_b: customerId,
      };

      try {
        const resp = await axios.post(
            `${SSLCZ_BASE_URL.value()}/gwprocess/v4/api.php`,
            qs.stringify(payload),
            {headers: {"Content-Type": "application/x-www-form-urlencoded"}},
        );

        if (resp.data.status !== "SUCCESS") {
          logger.error("SSLCommerz init failed", resp.data);
          return res.status(502).send({error: "Gateway init failed", detail: resp.data});
        }

        await db.ref(`Payments/${tranId}`).set({
          tranId,
          jobId,
          customerId,
          amount: Number(amount),
          method: "bKash",
          status: "pending",
          createdAt: admin.database.ServerValue.TIMESTAMP,
        });

        await db.ref(`Job/${jobId}`).update({
          paymentStatus: "processing",
          pendingTranId: tranId,
        });

        return res.send({
          GatewayPageURL: resp.data.GatewayPageURL,
          tranId,
        });
      } catch (e) {
        logger.error("initPayment error", e);
        return res.status(500).send({error: "Internal error"});
      }
    },
);

/**
 * POST /sslIpn
 * SSLCommerz server-to-server notification - authoritative result.
 */
exports.sslIpn = onRequest(
    {secrets: [SSLCZ_STORE_ID, SSLCZ_STORE_PASSWD]},
    async (req, res) => {
      const body = req.body || {};
      const tranId = body.tran_id;
      const valId = body.val_id;
      const status = body.status;

      if (!tranId || !valId) {
        logger.warn("IPN missing tran_id/val_id", body);
        return res.status(400).send("bad request");
      }

      try {
        const validateUrl =
          `${SSLCZ_BASE_URL.value()}/validator/api/validationserverAPI.php` +
          `?val_id=${encodeURIComponent(valId)}` +
          `&store_id=${encodeURIComponent(SSLCZ_STORE_ID.value())}` +
          `&store_passwd=${encodeURIComponent(SSLCZ_STORE_PASSWD.value())}` +
          `&format=json`;

        const valResp = await axios.get(validateUrl);
        const v = valResp.data;
        const ok = v && (v.status === "VALID" || v.status === "VALIDATED");

        const paymentRef = db.ref(`Payments/${tranId}`);
        const snap = await paymentRef.get();
        if (!snap.exists()) {
          logger.warn("IPN for unknown tran_id", tranId);
          return res.status(404).send("unknown");
        }
        const payment = snap.val();
        const amountMatches = Number(v.amount) === Number(payment.amount);

        if (ok && amountMatches && status === "VALID") {
          await paymentRef.update({
            status: "success",
            valId,
            bankTranId: v.bank_tran_id || null,
            cardIssuer: v.card_issuer || null,
            completedAt: admin.database.ServerValue.TIMESTAMP,
          });
          await db.ref(`Job/${payment.jobId}`).update({
            paymentStatus: "done",
            custpay: String(payment.amount),
            jobPaymentOption: "bKash",
            bkashTransactionId: v.bank_tran_id || tranId,
          });
        } else {
          await paymentRef.update({
            status: "failed",
            reason: ok ? "amount-mismatch" : (v && v.status) || "invalid",
            completedAt: admin.database.ServerValue.TIMESTAMP,
          });
          await db.ref(`Job/${payment.jobId}`).update({
            paymentStatus: "failed",
          });
        }

        return res.status(200).send("ok");
      } catch (e) {
        logger.error("IPN error", e);
        return res.status(500).send("error");
      }
    },
);

/**
 * GET /sslRedirect
 * Browser landing page after the user finishes at SSLCommerz.
 */
exports.sslRedirect = onRequest((req, res) => {
  const status = req.query.status || "unknown";
  res.set("Content-Type", "text/html");
  res.send(`
<!doctype html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Payment ${status}</title>
<style>
body{font-family:sans-serif;text-align:center;padding:40px;color:#333}
.box{max-width:320px;margin:0 auto}
</style></head>
<body><div class="box">
<h2>Payment ${status}</h2>
<p>You can close this window and return to the app.</p>
<script>
  setTimeout(function(){ window.location.href = "handymanapp://payment?status=${status}"; }, 800);
</script>
</div></body></html>`);
});
