package com.example.hronline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*

data class OrgNode(
    val name: String,
    val position: String,
    val department: String,
    val level: Int,
    val initials: String,
)

@Composable
fun StrukturOrgScreen(onBack: () -> Unit) {
    val orgData = remember {
        listOf(
            OrgNode("Ahmad Directur", "Direktur Utama", "Board", 0, "AD"),
            OrgNode("Budi Santoso", "VP Engineering", "IT & Development", 1, "BS"),
            OrgNode("Citra Dewi", "VP Finance", "Finance", 1, "CD"),
            OrgNode("Diana Putri", "VP HR", "Human Resources", 1, "DP"),
            OrgNode("Eko Prasetyo", "Manager IT", "IT & Development", 2, "EP"),
            OrgNode("Fajar Rahman", "Manager Finance", "Finance", 2, "FR"),
            OrgNode("Gita Lestari", "Manager HR", "Human Resources", 2, "GL"),
            OrgNode("Aries Adityanto", "Staff IT", "IT & Development", 3, "AA"),
            OrgNode("Hana Safitri", "Staff IT", "IT & Development", 3, "HS"),
            OrgNode("Ivan Kusuma", "Staff Finance", "Finance", 3, "IK"),
            OrgNode("Jessica Tan", "Staff HR", "Human Resources", 3, "JT"),
        )
    }

    val levelColors = mapOf(
        0 to AccentPurple,
        1 to AccentBlue,
        2 to GreenPrimary,
        3 to AccentOrange,
    )
    val levelBgColors = mapOf(
        0 to AccentPurpleLight,
        1 to AccentBlueLight,
        2 to Green50,
        3 to AccentOrangeLight,
    )
    val levelLabels = mapOf(
        0 to "Direktur",
        1 to "Vice President",
        2 to "Manager",
        3 to "Staff",
    )

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Struktur Organisasi", onBack = onBack)

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            levelLabels.forEach { (level, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(levelColors[level] ?: TextTertiary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(orgData) { node ->
                val color = levelColors[node.level] ?: TextTertiary
                val bgColor = levelBgColors[node.level] ?: SurfaceLight

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (node.level * 24).dp)
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarImage(initials = node.initials, size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(node.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(node.position, style = MaterialTheme.typography.labelSmall, color = color)
                    }
                    StatusBadge(
                        text = node.department,
                        type = when (node.level) {
                            0 -> BadgeType.NEUTRAL
                            1 -> BadgeType.INFO
                            2 -> BadgeType.SUCCESS
                            else -> BadgeType.WARNING
                        },
                    )
                }
            }
        }
    }
}
