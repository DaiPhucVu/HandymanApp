package com.example.handyman.chatbox.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun LoginButton(
    onClick: () -> Unit,
//    width: Dp = 330.dp,
//    height: Dp = 45.dp,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xffffb000)),
        contentPadding = PaddingValues(horizontal = 87.dp, vertical = 12.dp),
//        modifier = Modifier.requiredWidth(width).requiredHeight(height)
        modifier = modifier
    ) {
        Text(
            text = "Login",
            color = Color(0xff30386d),
            textAlign = TextAlign.Center,
            lineHeight = 1.25.em,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginButtonPreview() {
}
