package com.tezov.medium.adr.webview_local

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tezov.medium.adr.webview_local.ui.theme.Webview_LocalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            Webview_LocalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "screen_a") {
                        composable("screen_a") { ScreenA(navController) }
                        composable("screen_b") { ScreenB(navController) }
                        composable("screen_c") { ScreenC(navController) }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    navController:NavController,
){
    Row(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Button(
            modifier = Modifier.wrapContentHeight().weight(1.0f),
            onClick = {
                navController.navigate("screen_a") {
                    popUpTo(navController.currentDestination?.route ?:"") { inclusive = true }
                }
            }) {
            Text("Screen A")
        }
        Button(
            modifier = Modifier.wrapContentHeight().weight(1.0f),
            onClick = {
                navController.navigate("screen_b") {
                    popUpTo("screen_b") { inclusive = true }
                }
            }) {
            Text("Screen B")
        }
        Button(
            modifier = Modifier.wrapContentHeight().weight(1.0f),
            onClick = {
                navController.navigate("screen_c") {
                    popUpTo("screen_b") { inclusive = true }
                }
            }) {
            Text("Screen C")
        }
    }
}

@Composable
fun ScreenA(navController:NavController){
    WebView(
        modifier = Modifier
            .fillMaxSize(),
        rawHtmlResourceId = R.raw.html_splash,
        onUnavailable = {

            // webclient doesn't exist on device
            Text(text = "webclient doesn't exist on device")

        }
    ) {
        if (it == "onStart") {
            navController.navigate("screen_b") {
                popUpTo("screen_a") { inclusive = true }
            }
            true
        } else {
            false
        }
    }
}

@Composable
fun ScreenB(navController:NavController){
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {

        WebView(
            modifier = Modifier
                .fillMaxWidth().weight(1.0f),
            rawHtmlResourceId = R.raw.html_terms,
            onUnavailable = {

                // webclient doesn't exist on device
                Text(text = "webclient doesn't exist on device")

            }
        )
        BottomBar(navController)
    }
}

@Composable
fun ScreenC(navController:NavController){
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {

        WebView(
            modifier = Modifier
                .fillMaxWidth().weight(1.0f),
            rawHtmlResourceId = R.raw.html_not_implemented,
            placeholders = mutableMapOf("page_name" to "Placeholder replaced by SCREEN C"),
            onUnavailable = {

                // webclient doesn't exist on device
                Text(text = "webclient doesn't exist on device")

            }
        )
        BottomBar(navController)
    }
}