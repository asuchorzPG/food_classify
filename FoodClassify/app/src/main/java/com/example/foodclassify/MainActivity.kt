package com.example.foodclassify

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.foodclassify.databinding.ActivityMainBinding
import java.util.concurrent.Executors
import androidx.activity.viewModels
import androidx.lifecycle.Observer


class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val viewModel: MainViewModel by viewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission", "Granted")
            } else {
                Log.i("Permission", "Denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        requestPerm()
        if (viewModel.productList.isEmpty())
        {
            viewModel.loadData(this)
        }

        viewModel.currentResult.observe(this, Observer {
            viewBinding.LabelID.text = it.toString()
        })
        viewModel.currentMacro.observe(this, Observer {
            viewBinding.kcalTextID.text = it.kcal.toString()
            viewBinding.proteinTextID.text = it.protein.toString()
            viewBinding.carbsTextID.text = it.carbs.toString()
            viewBinding.fatTextID.text = it.fat.toString()
        })
    }


    private fun requestPerm() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(
                android.Manifest.permission.CAMERA
            )
        }
    }


    private  fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.previewViewID.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FoodClassifier(this) { res ->
                        Log.d("Model", "Produkt: $res")
                        viewModel.updateValue(res.label, res.score)
                    })
                }
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e("CameraISSUE", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}