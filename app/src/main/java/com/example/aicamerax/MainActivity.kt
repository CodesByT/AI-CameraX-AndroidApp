package com.example.aicamerax

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aicamerax.ui.theme.AiCameraXTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, cameraxPermissions, 0
            )
        }
        setContent {
            AiCameraXTheme {
                val scope = rememberCoroutineScope()
                val scaffoldState = rememberBottomSheetScaffoldState()
                val controller = remember {
                    LifecycleCameraController(
                        applicationContext
                    ).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
                        )
                    }
                }
                val viewModel = viewModel<MainViewModel>()
                val bitmaps by viewModel.bitmaps.collectAsState()
                BottomSheetScaffold(
                    modifier = Modifier,
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        BottomSheetContent(
                            bitmaps = bitmaps,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding).clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                    ) {
                        CameraPreview(
                            controller = controller,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                controller.cameraSelector =
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    else
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                            },
                            modifier = Modifier.offset(16.dp, 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Camera Rotation ",
                                tint = Color.White
                            )

                        }
                        Row(
                            modifier = Modifier
                                .padding(16.dp).padding(bottom = 20.dp)
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                },
                                modifier = Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = "Access photos",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = {
                                    takePhoto(
                                        controller = controller,
                                        onPhotoTaken = viewModel::onTakePhoto
                                    )
                                },
                                modifier = Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Click Image ",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoCall,
                                    contentDescription = "Record",
                                    tint = Color.White
                                )
                            }

                        }
                    }

                }

            }
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    onPhotoTaken(image.toBitmap())
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Photo taking error", exception)
                }
            }
        )
    }

    private fun requiredPermissions(): Boolean {
        return cameraxPermissions.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val cameraxPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}
