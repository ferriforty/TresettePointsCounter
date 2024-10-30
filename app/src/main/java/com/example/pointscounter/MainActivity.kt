/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.pointscounter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pointscounter.model.Model
import com.example.pointscounter.model.modelOf
import com.example.pointscounter.ui.theme.TipTimeTheme
import kotlinx.serialization.*
import kotlinx.serialization.json.*

const val DEFAULT_VALUE = 0
const val GRID_LENGTH = 8
var inputGrid = mutableStateListOf<Array<MutableState<Int>>>()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreference = this.getSharedPreferences("gameMemory", Context.MODE_PRIVATE)

        loadFromSharedPreference(sharedPreference)
        setContent {
            TipTimeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    PointsCounterLayout(inputGrid.size, modelOf(inputGrid), modifier = Modifier.padding(0.dp, 10.dp), sharedPreference)
                }
            }
        }
    }
}

fun loadFromSharedPreference(sharedPreferences: SharedPreferences) {
    val gameState = sharedPreferences.getString("gameState", "")
    if (gameState == "" || gameState == null) {
        for (i in 0 until GRID_LENGTH) {
            inputGrid.add(arrayOf(mutableStateOf(DEFAULT_VALUE),
                mutableStateOf(DEFAULT_VALUE),
                mutableStateOf(DEFAULT_VALUE),
                mutableStateOf(DEFAULT_VALUE)))
        }
        return
    }
    val list = Json.decodeFromString<List<List<Int>>>(gameState)
    for (x in list.indices) {
        inputGrid.add(arrayOf(mutableStateOf(list[x][0]),
            mutableStateOf(list[x][1]),
            mutableStateOf(list[x][2]),
            mutableStateOf(list[x][3])))
    }
}

@Composable
fun PointsCounterLayout(length: Int, model: Model, modifier: Modifier = Modifier, sharedPreference: SharedPreferences) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.verticalScroll(scrollState)) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1F)) {
                TeamText(text = stringResource(R.string.team_1), modifier = Modifier.fillMaxWidth())
                PointsSumText(sum = model::getTeam1Points, modifier = Modifier.fillMaxWidth())
                Row {
                    Column(modifier = Modifier.weight(1F)) {
                        PointsLabelText(stringResource(id = R.string.points), Modifier.fillMaxWidth())
                        val j = 0
                        repeat(length) {
                            EditPointField(Modifier.padding(5.dp), inputGrid[it][j], inputGrid[it][j + 2], sharedPreference)
                        }
                    }
                    Column(modifier = Modifier.weight(1F)) {
                        PointsLabelText(stringResource(id = R.string.extra_points), Modifier.fillMaxWidth())
                        repeat(length) {
                            val j = 1
                            EditExtraPointField(Modifier.padding(5.dp), inputGrid[it][j], sharedPreference)
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1F)) {
                TeamText(text = stringResource(R.string.team_2), modifier = Modifier.fillMaxWidth())
                PointsSumText(sum = model::getTeam2Points, modifier = Modifier.fillMaxWidth())
                Row {
                    Column(modifier = Modifier.weight(1F)) {
                        PointsLabelText(stringResource(id = R.string.points), Modifier.fillMaxWidth())
                        repeat(length) {
                            val j = 2
                            EditPointField(Modifier.padding(5.dp), inputGrid[it][j], inputGrid[it][j - 2], sharedPreference)
                        }
                    }
                    Column(modifier = Modifier.weight(1F)) {
                        PointsLabelText(stringResource(id = R.string.extra_points), Modifier.fillMaxWidth())
                        repeat(length) {
                            val j = 3
                            EditExtraPointField(Modifier.padding(5.dp), inputGrid[it][j], sharedPreference)
                        }
                    }
                }
            }
        }
        Row {
            ClearGridButton(modifier = Modifier.padding(10.dp), sharedPreference)
            Spacer(modifier = Modifier.width(10.dp))
            NewColButton(modifier = Modifier
                .padding(10.dp, 10.dp, 5.dp, 10.dp)
                .weight(1F), sharedPreference)
            DelColButton(modifier = Modifier
                .padding(5.dp, 10.dp, 10.dp, 10.dp)
                .weight(1F), sharedPreference)
        }
    }
}

@Composable
private fun PointsLabelText(text: String, modifier: Modifier) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun TeamText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun EditPointField(modifier: Modifier = Modifier, thisTeamPoints: MutableState<Int>, otherTeamPoints: MutableState<Int>, sharedPreference: SharedPreferences) {
    TextField(
        onValueChange = {
            try {
                thisTeamPoints.value = Integer.parseInt(it)
                if (thisTeamPoints.value > 11) {
                    thisTeamPoints.value = DEFAULT_VALUE
                    otherTeamPoints.value = DEFAULT_VALUE
                } else {
                    otherTeamPoints.value = 11 - thisTeamPoints.value
                }
                sharedPreference.edit().putString("gameState", Json.encodeToString(inputGrid.map { x -> x.map { y -> y.value } })).apply()
            } catch (_: NumberFormatException) {
                thisTeamPoints.value = DEFAULT_VALUE
                otherTeamPoints.value = DEFAULT_VALUE
            }
        },
        value = if (thisTeamPoints.value == DEFAULT_VALUE)  "" else thisTeamPoints.value.toString(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        textStyle = TextStyle(textAlign = TextAlign.Center),
        minLines = 1,
        maxLines = 1,
    )
}

@Composable
fun EditExtraPointField(modifier: Modifier = Modifier, mutableState: MutableState<Int>, sharedPreference: SharedPreferences) {
    TextField(
        onValueChange = {
            try {
                mutableState.value = Integer.parseInt(it)
                sharedPreference.edit().putString("gameState", Json.encodeToString(inputGrid.map { x -> x.map { y -> y.value } })).apply()
            } catch (_: NumberFormatException) {
                mutableState.value = DEFAULT_VALUE
            }
        },
        value = if (mutableState.value == DEFAULT_VALUE)  "" else mutableState.value.toString(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        textStyle = TextStyle(textAlign = TextAlign.Center),
        minLines = 1,
        maxLines = 1,
    )
}

@Composable
fun PointsSumText(modifier: Modifier = Modifier, sum: () -> Int) {
    Text(
        text = sum().toString(),
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun NewColButton(modifier: Modifier = Modifier, sharedPreference: SharedPreferences) {
    Button(
        onClick = {
            inputGrid.add(arrayOf(mutableStateOf(DEFAULT_VALUE),
                mutableStateOf(DEFAULT_VALUE),
                mutableStateOf(DEFAULT_VALUE),
                mutableStateOf(DEFAULT_VALUE)))
            sharedPreference.edit().putString("gameState", Json.encodeToString(inputGrid.map { x -> x.map { y -> y.value } })).apply()

        },
        modifier = modifier
    ) {
        Text(text = stringResource(id = R.string.new_col))
    }
}

@Composable
fun DelColButton(modifier: Modifier = Modifier, sharedPreference: SharedPreferences) {
    Button(
        enabled = inputGrid.size > 6,
        onClick = {
            inputGrid.removeLast()
            sharedPreference.edit().putString("gameState", Json.encodeToString(inputGrid.map { x -> x.map { y -> y.value } })).apply()
        },
        modifier = modifier
    ) {
        Text(text = stringResource(id = R.string.del_col))
    }
}

@Composable
fun ClearGridButton(modifier: Modifier = Modifier, sharedPreference: SharedPreferences) {
    Button(
        onClick = {
        for (row in inputGrid) {
            for (cell in row) {
                cell.value = 0
            }
        }
        sharedPreference.edit().putString("gameState", "").apply()
    },
        modifier = modifier
    ) {
        Text(text = stringResource(id = R.string.new_game))
    }
}

/*
@Preview(showBackground = true,
    device = "id:pixel", showSystemUi = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        PointsCounterLayout(inputGrid.size, modelOf(inputGrid), Modifier.padding(0.dp, 10.dp), this)
    }
}
*/