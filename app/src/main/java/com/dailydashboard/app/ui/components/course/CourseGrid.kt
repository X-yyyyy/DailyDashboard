package com.dailydashboard.app.ui.components.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailydashboard.app.data.model.Course
import com.dailydashboard.app.data.model.TimeSlot
import com.dailydashboard.app.util.SemesterCalculator

private val dayHeaders = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

@Composable
fun CourseGrid(
    courses: List<Course>,
    timeSlots: List<TimeSlot>,
    currentWeek: Int,
    modifier: Modifier = Modifier,
    onCourseClick: (Course) -> Unit = {},
) {
    val defaultSlots = if (timeSlots.isEmpty()) {
        (1..12).map { TimeSlot(slot = it, startTime = "${8 + it - 1}:00", endTime = "${8 + it}:00") }
    } else timeSlots.sortedBy { it.slot }

    val slotHeight = 80.dp
    val dayWidth = 80.dp
    val timeColWidth = 48.dp

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Row(modifier = Modifier.height(40.dp).fillMaxWidth()) {
                dayHeaders.forEachIndexed { index, header ->
                    Box(
                        modifier = Modifier
                            .width(if (index == 0) timeColWidth else dayWidth)
                            .fillMaxHeight()
                            .background(
                                if (index == 0) Color.Transparent
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (index > 0) {
                            Text(
                                header,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }

        itemsIndexed(defaultSlots) { slotIndex, slot ->
            val slotCourses = courses.filter { course ->
                course.schedules.any { s ->
                    s.startSlot <= slot.slot &&
                    slot.slot < s.startSlot + s.duration &&
                    SemesterCalculator.shouldShowCourse(s.weekType, currentWeek, s.customWeeks)
                }
            }

            Box(
                modifier = Modifier
                    .height(slotHeight)
                    .fillMaxWidth()
                    .background(
                        if (slotIndex % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        else Color.Transparent
                    ),
            ) {
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    Box(
                        modifier = Modifier.width(timeColWidth).fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text(
                            slot.startTime,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    (1..7).forEach { day ->
                        val dayCourses = slotCourses.filter { course ->
                            course.schedules.any { s -> s.dayOfWeek == day && s.startSlot == slot.slot }
                        }

                        Box(
                            modifier = Modifier
                                .width(dayWidth)
                                .fillMaxHeight(),
                        ) {
                            dayCourses.forEach { course ->
                                val duration = course.schedules
                                    .firstOrNull { it.dayOfWeek == day && it.startSlot == slot.slot }
                                    ?.duration ?: 1
                                val color = Color(android.graphics.Color.parseColor(
                                    course.color.ifBlank { "#8A9A5B" }
                                ))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(slotHeight * duration)
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color.copy(alpha = 0.85f))
                                        .padding(2.dp),
                                ) {
                                    Column {
                                        Text(
                                            course.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 11.sp,
                                        )
                                        Text(
                                            course.location,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 9.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )
            }
        }
    }
}
