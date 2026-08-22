## Project Structure

```
app
├── sampledata
├── manifests
│   └── AndroidManifest.xml
├── kotlin+java
│   └── com.example.handyman
│       ├── chatbox
│       │   ├── ui.composables
│       │   │   ├── ChatClientActivity
│       │   │   ├── ChatListingActivity
│       │   │   └── MainActivity
│       ├── components
│       │   └── KYCComponents.kt
│       ├── customer_pages
│       │   ├── CustomerHome.kt
│       │   ├── CustomerHomeKYCProcessing.kt
│       │   ├── CustomerHomeUnverified.kt
│       │   ├── CustomerKYCAddressForm.kt
│       │   ├── CustomerKYCCaptureID.kt
│       │   ├── CustomerKYCCodeOTP.kt
│       │   ├── CustomerKYCLanding.kt
│       │   ├── CustomerKYCPhoneNumber.kt
│       │   ├── CustomerKYCSubmitted.kt
│       │   ├── CustomerKYCSuccess.kt
│       │   ├── CustomerLogin.kt
│       │   └── CustomerSignup.kt
│       ├── handyman_pages
│       │   ├── HandymanHome.kt
│       │   ├── HandymanHomePageUnverified.kt
│       │   ├── HandymanKYCAddressForm.kt
│       │   ├── HandymanKYCCaptureID.kt
│       │   ├── HandymanKYCCodeOTP.kt
│       │   ├── HandymanKYCLanding.kt
│       │   ├── HandymanKYCPhoneNumber.kt
│       │   ├── HandymanKYCSuccess.kt
│       │   ├── HandymanLogin.kt
│       │   └── HandymanSignup.kt
│       ├── ui.theme
│       │   ├── Color.kt
│       │   ├── Theme.kt
│       │   └── Type.kt
│       ├── utils
│       │    ├── SessionManager
│       ├── ChooseAccountType.kt
│       ├── ChooseAccountTypeActivity
│       ├── CustomerJobDetailsFragment
│       ├── CustomerJobListAdapter
│       ├── CustomerJobListDiff
│       ├── CustomerJobListFragment
│       ├── CustomerJobPaymentFragment
│       ├── HandymanJobBoardAdapter
│       ├── HandymanJobBoardDetailsFragment
│       ├── HandymanJobBoardDiff
│       ├── HandymanJobBoardFragment
│       ├── HandymanJobListAdapter
│       ├── HandymanJobListDetailsFragment
│       ├── HandymanJobListDiff
│       ├── HandymanJobListFragment
│       ├── Job
│       ├── JobEditFragment
│       ├── JobPostingFragment
│       ├── JobRequestDoneFragment
│       ├── JobViewModel
│       ├── LandingPage.kt
│       ├── MainJobBoard
│       ├── Navigation.kt
│       ├── OrderSummaryFragment
│       ├── PaymentSuccessFragment
│       ├── ServiceCategory
│       ├── ServiceCategoryAdapter
│       ├── ServiceCategoryDiff
│       ├── ServiceCategoryFragment.kt
│       ├── ServiceCategoryViewModel
│       └── SupportForm.kt
├── res
│   ├── drawable
│   ├── layout
│   │   ├── activity_support_form.xml
│   │   ├── customer_job_list_item.xml
│   │   ├── dialog_payment_input.xml
│   │   ├── fragment_customer_job_details.xml
│   │   ├── fragment_customer_job_list.xml
│   │   ├── fragment_customer_job_payment.xml
│   │   ├── fragment_handyman_job_board.xml
│   │   ├── fragment_handyman_job_board_details.xml
│   │   ├── fragment_handyman_job_list.xml
│   │   ├── fragment_handyman_job_list_details.xml
│   │   ├── fragment_job_edit.xml
│   │   ├── fragment_job_posting.xml
│   │   ├── fragment_job_request_done.xml
│   │   ├── fragment_order_summary.xml
│   │   ├── fragment_payment_success.xml
│   │   ├── fragment_service_category.xml
│   │   ├── handyman_job_board_item.xml
│   │   ├── handyman_job_list_item.xml
│   │   ├── job_board_main.xml
│   │   ├── photo_thumbnail.xml
│   │   └── service_category_item.xml
│   ├── mipmap
│   ├── navigation
│   │   └── nav_graph.xml
│   ├── values
│   └── xml
├── androidTest
│   └── com.example.handyman
├── test
│   └── com.example.handyman
├── build.gradle (Project: Handyman)
├── build.gradle (Module: app)
├── proguard-rules.pro
├── gradle.properties
├── gradle-wrapper.properties
├── local.properties
└── settings.gradle
```
