package com.example.tipcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TipCalcScreen()
                }
            }
        }
    }
}

@Composable
fun TipCalcScreen() {
    // Состояния для полей ввода
    var billAmount by remember { mutableStateOf("") }
    var dishesCount by remember { mutableStateOf("") }
    var tipPercent by remember { mutableStateOf(10f) } // Слайдер от 0 до 25

    // Индекс выбранной радиокнопки (-1 = ничего не выбрано)
    var selectedDiscountIndex by remember { mutableStateOf(-1) }

    // Состояния для результатов
    var tipAmountResult by remember { mutableStateOf(0.0) }
    var discountAmountResult by remember { mutableStateOf(0.0) }
    var totalResult by remember { mutableStateOf(0.0) }

    // Логика автоматического выбора скидки и подсчёта итога
    LaunchedEffect(billAmount, dishesCount, tipPercent) {
        val dishes = dishesCount.toIntOrNull() ?: 0
        val bill = billAmount.toDoubleOrNull() ?: 0.0

        // 1. Определяем скидку и подсвечиваем нужную радиокнопку
        val discountPercent = when {
            dishes in 1..2 -> { selectedDiscountIndex = 0; 3.0 }
            dishes in 3..5 -> { selectedDiscountIndex = 1; 5.0 }
            dishes in 6..10 -> { selectedDiscountIndex = 2; 7.0 }
            dishes > 10 -> { selectedDiscountIndex = 3; 10.0 }
            else -> { selectedDiscountIndex = -1; 0.0 }
        }

        // 2. Считаем суммы
        if (bill > 0.0) {
            tipAmountResult = bill * (tipPercent / 100.0)
            discountAmountResult = bill * (discountPercent / 100.0)
            totalResult = bill + tipAmountResult - discountAmountResult
        } else {
            tipAmountResult = 0.0
            discountAmountResult = 0.0
            totalResult = 0.0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Калькулятор скидки",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле суммы (ограничение ввода: только цифры и точка/запятая)
        OutlinedTextField(
            value = billAmount,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                    billAmount = newValue.replace(',', '.')
                }
            },
            label = { Text("Сумма счёта") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Поле количества блюд (ограничение ввода: только цифры)
        OutlinedTextField(
            value = dishesCount,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                    dishesCount = newValue
                }
            },
            label = { Text("Количество блюд") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Слайдер
        Text(text = "Чаевые: ${tipPercent.toInt()}%", fontSize = 16.sp)
        Slider(
            value = tipPercent,
            onValueChange = { tipPercent = it },
            valueRange = 0f..25f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Радиокнопки
        Text(text = "Скидка (выбирается автоматически):", fontSize = 16.sp)

        Column(modifier = Modifier.fillMaxWidth()) {
            DiscountRadioButton(text = "3% (1-2 блюда)", selected = selectedDiscountIndex == 0)
            DiscountRadioButton(text = "5% (3-5 блюд)", selected = selectedDiscountIndex == 1)
            DiscountRadioButton(text = "7% (6-10 блюд)", selected = selectedDiscountIndex == 2)
            DiscountRadioButton(text = "10% (>10 блюд)", selected = selectedDiscountIndex == 3)
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // Блок вывода результатов
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Чаевые: ${"%.2f".format(tipAmountResult)} руб.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Скидка: -${"%.2f".format(discountAmountResult)} руб.",
                fontSize = 16.sp,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Итого: ${"%.2f".format(totalResult)} руб.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Вспомогательный компонент для радиокнопки
@Composable
fun DiscountRadioButton(text: String, selected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(text = text, fontSize = 16.sp)
    }
}