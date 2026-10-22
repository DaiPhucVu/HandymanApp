package com.example.handyman.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.handyman.R
import java.io.File

@Composable
fun StepCircle(stepNumber: Int, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (isActive) Color(0xFFFFB703) else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color(0xFFFFB703),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stepNumber.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.White else Color(0xFFFFB703)
        )
    }
}

@Composable
fun DividerLine() {
    Box(
        modifier = Modifier
            .height(1.dp)
            .width(32.dp)
            .background(Color(0xFFFFB703))
    )
}

@Composable
fun CameraPreviewView(
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraPreviewView", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        IDCardOverlay()

        Text(
            text = stringResource(id = R.string.place_id_card_hint),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun IDCardOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 2.dp.toPx()
        val cornerRadius = 12.dp.toPx()
        
        // Calculate the rectangle for the ID card (standard aspect ratio ~1.58)
        val rectWidth = size.width * 0.85f
        val rectHeight = rectWidth / 1.58f
        val rectLeft = (size.width - rectWidth) / 2
        val rectTop = (size.height - rectHeight) / 2
        
        val rect = Rect(offset = Offset(rectLeft, rectTop), size = Size(rectWidth, rectHeight))
        
        // Create a path for the transparent hole
        val path = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(cornerRadius, cornerRadius)))
        }

        // Draw the semi-transparent overlay with a hole
        clipPath(path, clipOp = ClipOp.Difference) {
            drawRect(color = Color.Black.copy(alpha = 0.5f))
        }

        // Draw the frame border
        drawRoundRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(rectLeft, rectTop),
            size = Size(rectWidth, rectHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth)
        )
    }
}

fun takePhoto(
    imageCapture: ImageCapture,
    context: Context,
    onPhotoCaptured: (Uri) -> Unit
) {
    val photoFile = try {
        File(
            context.cacheDir,
            "KYC_ID_${System.currentTimeMillis()}.jpg"
        ).apply {
            if (exists()) delete()
            createNewFile()
        }
    } catch (e: Exception) {
        Log.e("CameraCapture", "Failed to create photo file", e)
        return
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onPhotoCaptured(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraCapture", "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}
