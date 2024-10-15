package com.delta.restart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.wear.compose.material.*
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DirectoryListScreen(filesDir, this)
        }
    }
}

@Composable
fun DirectoryListScreen(filesDir: File, lifecycleOwner: LifecycleOwner) {
    // State to hold the directory information (pair of directory name and size)
    var directorySizes by remember { mutableStateOf(emptyList<Pair<String, String>>()) }

    // Use DisposableEffect to listen to the Lifecycle and trigger size calculation on onResume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                directorySizes = getDirectoriesWithSizes(filesDir)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Cleanup the observer when the composable leaves the composition
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Wear Compose UI with ScalingLazyColumn
    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(directorySizes) { directory ->
                DirectoryItem(directory.first, directory.second)
            }
        }
    }
}

@Composable
fun DirectoryItem(directoryName: String, size: String) {
    val formattedDirectoryName = parseDirectoryName(directoryName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Display formatted directory name (MM-DD HH:mm)
        Text(text = formattedDirectoryName, style = MaterialTheme.typography.body1)

        // Display size in red
        Text(
            text = "Size: $size",
            style = MaterialTheme.typography.body2,
            color = Color.Red
        )
    }
}

// Function to retrieve directories and their sizes in reverse order
fun getDirectoriesWithSizes(filesDir: File): List<Pair<String, String>> {
    val directories = mutableListOf<Pair<String, String>>()

    filesDir.listFiles()?.sortedByDescending { it.name }?.forEach { file ->
        if (file.isDirectory) {
            val dirSize = getDirectorySize(file)
            directories.add(file.name to formatSize(dirSize))
        }
    }

    return directories
}

// Function to calculate the size of a directory recursively
fun getDirectorySize(directory: File): Long {
    var size: Long = 0
    directory.listFiles()?.forEach { file ->
        size += if (file.isFile) {
            file.length()
        } else {
            getDirectorySize(file)
        }
    }
    return size
}

// Function to format the size in human-readable format (KB, MB, GB)
fun formatSize(size: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        size >= gb -> String.format("%.2f GB", size.toDouble() / gb)
        size >= mb -> String.format("%.2f MB", size.toDouble() / mb)
        size >= kb -> String.format("%.2f KB", size.toDouble() / kb)
        else -> "$size B"
    }
}

// Function to parse the directory name from format YYYY-MM-DD_HH_mm_ss to MM-DD HH:mm
fun parseDirectoryName(directoryName: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault())
        val date = dateFormat.parse(directoryName)
        val outputFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        outputFormat.format(date!!)
    } catch (e: Exception) {
        directoryName  // Return original name if parsing fails
    }
}