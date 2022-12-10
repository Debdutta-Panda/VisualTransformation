package com.debduttapanda.visualtransformation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.debduttapanda.visualtransformation.ui.theme.VisualTransformationTheme
import java.text.DecimalFormat
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VisualTransformationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var text by remember {
                        mutableStateOf("")
                    }
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        TextField(
                            value = text,
                            onValueChange = {
                                text = it
                            },
                            visualTransformation = VisualTransformation.None
                        )
                    }
                }
            }
        }
    }
}

fun creditCard(text: AnnotatedString): TransformedText{
    // Making XXXX-XXXX-XXXX-XXXX string.
    val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
    var out = ""
    for (i in trimmed.indices) {
        out += trimmed[i]
        if (i % 4 == 3 && i != 15) out += "-"
    }

    /**
     * The offset translator should ignore the hyphen characters, so conversion from
     *  original offset to transformed text works like
     *  - The 4th char of the original text is 5th char in the transformed text.
     *  - The 13th char of the original text is 15th char in the transformed text.
     *  Similarly, the reverse conversion works like
     *  - The 5th char of the transformed text is 4th char in the original text.
     *  - The 12th char of the transformed text is 10th char in the original text.
     */
    val creditCardOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 7) return offset + 1
            if (offset <= 11) return offset + 2
            if (offset <= 16) return offset + 3
            return 19
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 4) return offset
            if (offset <= 9) return offset - 1
            if (offset <= 14) return offset - 2
            if (offset <= 19) return offset - 3
            return 16
        }
    }

    return TransformedText(AnnotatedString(out), creditCardOffsetTranslator)
}

fun mobileNumberFilter(text: AnnotatedString): TransformedText {
    val mask = "xx xxx xx xx"
    // change the length
    val trimmed =
        if (text.text.length >= 9) text.text.substring(0..8) else text.text

    val annotatedString = AnnotatedString.Builder().run {
        for (i in trimmed.indices) {
            append(trimmed[i])
            if (i == 1 || i == 4 || i == 6) {
                append(" ")
            }
        }
        pushStyle(SpanStyle(color = Color.LightGray))
        append(mask.takeLast(mask.length - length))
        toAnnotatedString()
    }

    val phoneNumberOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 1) return offset
            if (offset <= 4) return offset + 1
            if (offset <= 6) return offset + 2
            if (offset <= 9) return offset + 3
            return 12
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 1) return offset
            if (offset <= 4) return offset - 1
            if (offset <= 6) return offset - 2
            if (offset <= 9) return offset - 3
            return 9
        }
    }

    return TransformedText(annotatedString, phoneNumberOffsetTranslator)
}

fun Onedot(it: AnnotatedString): TransformedText{
    return TransformedText(
        AnnotatedString.Builder().run {
            it.forEach {
                append(it)
                append(".")
            }
            toAnnotatedString()
        },
        object: OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset * 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                return offset / 2
            }
        }
    )
}

fun Static(it: AnnotatedString): TransformedText{
    return TransformedText(
        AnnotatedString("Jetpack Compose is great".repeat(100).take(it.length)),
        OffsetMapping.Identity
    )
}

object DateVisualTransformation : VisualTransformation {
    fun transform(original: String): String {
        val trimmed: String = original.take(8)
        if (trimmed.length < 4) return trimmed
        if (trimmed.length == 4) return "$trimmed-"
        val (year, monthAndOrDate) = trimmed.chunked(4)
        if (trimmed.length == 5 ) return "$year-$monthAndOrDate"
        if(trimmed.length == 6) return "$year-$monthAndOrDate-"
        val (month, date) = monthAndOrDate.chunked(2)
        return "$year-$month-$date"
    }

    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(AnnotatedString(transform(text.text)),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 3) return offset
                    if (offset <= 5) return offset + 1
                    if (offset <= 7) return offset + 2
                    return 10
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 4) return offset
                    if (offset <= 7) return offset - 1
                    if (offset <= 10) return offset - 2
                    return 8
                }
            })
    }
}

class DateVisualTransformationddmmyyyy : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Make the string DD-MM-YYYY
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var output = ""
        for (i in trimmed.indices) {
            output += trimmed[i]
            if (i < 4 && i % 2 == 1) output += "-"
        }
        val dateTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // [offset [0 - 1] remain the same]
                if (offset <= 1) return offset
                // [2 - 3] transformed to [3 - 4] respectively
                if (offset <= 3) return offset + 1
                // [4 - 7] transformed to [6 - 9] respectively
                if (offset <= 7) return offset + 2
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset - 1
                if (offset <= 9) return offset - 2
                return 8
            }

        }

        return TransformedText(
            AnnotatedString(output),
            dateTranslator
        )
    }
}

class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {

        val symbols = DecimalFormat().decimalFormatSymbols
        val decimalSeparator = symbols.decimalSeparator

        var outputText = ""
        var integerPart = 0L
        var decimalPart = ""

        if (text.text.isNotEmpty()) {
            val number = text.text.toDouble()
            integerPart = number.toLong()
            outputText += NumberFormat.getIntegerInstance().format(integerPart)
            if (text.text.contains(decimalSeparator)) {
                decimalPart = text.text.substring(text.text.indexOf(decimalSeparator))
                if (decimalPart.isNotEmpty()) {
                    outputText += decimalPart
                }
            }
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return outputText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return text.length
            }
        }

        return TransformedText(
            text = AnnotatedString(outputText),
            offsetMapping = numberOffsetTranslator
        )
    }
}

private fun String.withThousands(separator: Char = ','): String {
    val original = this
    return buildString {
        original.indices.forEach { position ->
            val realPosition = original.lastIndex - position
            val character = original[realPosition]
            insert(0, character)
            if (position != 0 && realPosition != 0 && position % 3 == 2) {
                insert(0, separator)
            }
        }
    }
}

fun priceFilter(
    text: String,
    thousandSeparator: (String) -> String = { text -> text.withThousands() },
): TransformedText {
    val out = thousandSeparator(text)
    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val rightOffset = text.lastIndex - offset
            val commasToTheRight = rightOffset / 3
            return out.lastIndex - rightOffset - commasToTheRight
        }

        override fun transformedToOriginal(offset: Int): Int {
            val totalCommas = ((text.length - 1) / 3).coerceAtLeast(0)
            val rightOffset = out.length - offset
            val commasToTheRight = rightOffset / 4
            return (offset - (totalCommas - commasToTheRight))
        }
    }
    return TransformedText(AnnotatedString(out), offsetMapping)
}