package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.util.ParsedUpiData
import com.example.util.UpiQrParser
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun LiveQrScannerScreen(
    onQrScanned: (ParsedUpiData) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery Picker for QR Screenshots (e.g. sent on WhatsApp/Telegram)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        if (bitmap != null) {
                            val inputImage = InputImage.fromBitmap(bitmap, 0)
                            val scanner = BarcodeScanning.getClient()
                            scanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    val qr = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                                    val raw = qr?.rawValue
                                    if (!raw.isNullOrBlank()) {
                                        triggerHaptic(context)
                                        val parsed = UpiQrParser.parse(raw)
                                        onQrScanned(parsed)
                                    }
                                }
                        }
                    }
                } catch (_: Exception) {
                    // Graceful fallback
                }
            }
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            CameraPreviewAndScanner(
                onQrDetected = { raw ->
                    triggerHaptic(context)
                    val parsed = UpiQrParser.parse(raw)
                    onQrScanned(parsed)
                }
            )
        } else {
            // Permission Request State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = SatarkAccentAlpha,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SatarkPay scans UPI QR codes on-device to inspect recipient handles and block coercion before money is sent.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("Select QR Screenshot from Gallery", color = SatarkAccent)
                }
            }
        }

        // Top Header Overlay
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("qr_scanner_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Scan UPI QR Code",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.testTag("qr_gallery_pick")) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick Image", tint = Color.White)
                }
            }
        }

        // Bottom Controls Overlay
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Point camera at any merchant or payment QR code",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "🛡️ Instant on-device attack-chain analysis before UPI PIN entry",
                    color = SatarkOk,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreviewAndScanner(
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanned by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    // Laser Animation Line
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val executor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val barcodeScanner = BarcodeScanning.getClient(options)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !isScanned) {
                            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    val qr = barcodes.firstOrNull()
                                    val rawValue = qr?.rawValue
                                    if (!rawValue.isNullOrBlank() && !isScanned) {
                                        isScanned = true
                                        onQrDetected(rawValue)
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera
                    } catch (_: Exception) {
                        // Camera binding fallback
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Center Viewfinder Target
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .border(2.dp, SatarkAccent, RoundedCornerShape(16.dp))
        ) {
            // Animated Scanning Laser Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = laserOffsetY.dp)
                    .background(SatarkAccent)
            )
        }

        // Flashlight / Torch Toggle Floating Button
        IconButton(
            onClick = {
                val next = !isTorchOn
                isTorchOn = next
                cameraControl?.cameraControl?.enableTorch(next)
            },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 160.dp)
                .clip(CircleShape)
                .background(if (isTorchOn) SatarkAccent else Color.Black.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Flashlight",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun triggerHaptic(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            v?.vibrate(80)
        }
    } catch (_: Exception) {
        // Haptic fallback
    }
}
