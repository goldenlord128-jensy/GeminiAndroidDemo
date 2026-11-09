package com.goldenlord.geminiandroiddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GeminiDemoScreen()
                }
            }
        }
    }
}

@Composable
fun GeminiDemoScreen() {
    var apiKey by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("Explain asynchronous programming in Kotlin in 2 sentences.") }
    var outputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gemini API Kotlin Sample",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("Gemini API Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Prompt Input") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Button(
            onClick = {
                if (apiKey.isBlank()) {
                    outputText = "Error: Please enter a valid Gemini API key."
                    return@Button
                }
                isLoading = true
                outputText = ""
                scope.launch {
                    val modelsToTry = listOf("gemini-3.8-flash", "gemini-1.5-flash", "gemini-2.0-flash")
                    var success = false

                    for (modelName in modelsToTry) {
                        try {
                            val generativeModel = GenerativeModel(
                                modelName = modelName,
                                apiKey = apiKey
                            )
                            val response = generativeModel.generateContent(prompt)
                            outputText = "Output (via $modelName):\n\n${response.text ?: "No response received."}"
                            success = true
                            break // Exit loop if successful
                        } catch (e: Exception) {
                            // If server is unavailable (503) or model deprecated (404), try next model in fallback list
                            continue
                        }
                    }

                    if (!success) {
                        outputText = "Error: Server high demand (503). Please wait a few seconds and try again."
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Response")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        if (outputText.isNotEmpty()) {
            Text(
                text = outputText,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}