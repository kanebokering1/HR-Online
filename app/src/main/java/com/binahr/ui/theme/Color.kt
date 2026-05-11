package com.binahr.ui.theme


import com.binahr.BuildConfig
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════
// BINA HR — Soft Orange Brand Color System
// ═══════════════════════════════════════════

// ── Orange Primary Palette (Brand) ──────────
val OrangePrimary  = Color(0xFFE07540)   // soft warm terracotta orange
val OrangeHover    = Color(0xFFC5622E)   // pressed / dark variant
val OrangeLight    = Color(0xFFF5C4AE)   // chip, secondary accent
val OrangeSurface  = Color(0xFFFDF1EB)   // container bg, card tint
val OrangeWarm     = Color(0xFFFFFAF7)   // warm page background tint

// ── Emerald (kept for success/status states only) ──
val Emerald50  = Color(0xFFECFDF5)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald200 = Color(0xFFA7F3D0)
val Emerald300 = Color(0xFF6EE7B7)
val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)
val Emerald700 = Color(0xFF047857)
val Emerald800 = Color(0xFF065F46)
val Emerald900 = Color(0xFF064E3B)

// Aliases — primary/brand now maps to orange
val GreenPrimary  = OrangePrimary       // backward compat
val GreenLight    = OrangeLight         // backward compat
val GreenDark     = OrangeHover         // backward compat
val TealAccent    = OrangePrimary       // backward compat
val TealLight     = OrangeLight
val TealDark      = OrangeHover
// Legacy shade aliases (emerald kept for status use)
val Green50       = Emerald50
val Green100      = Emerald100

// ── Accent Colors ────────────────────────────
val AccentBlue       = Color(0xFF3B82F6)
val AccentBlueDark   = Color(0xFF1D4ED8)
val AccentBlueLight  = Color(0xFFEFF6FF)

val AccentPurple      = Color(0xFF8B5CF6)
val AccentPurpleDark  = Color(0xFF6D28D9)
val AccentPurpleLight = Color(0xFFF5F3FF)

val AccentOrange      = Color(0xFFF59E0B)
val AccentOrangeDark  = Color(0xFFD97706)
val AccentOrangeLight = Color(0xFFFFFBEB)

val AccentRed      = Color(0xFFEF4444)
val AccentRedDark  = Color(0xFFDC2626)
val AccentRedLight = Color(0xFFFEF2F2)

val AccentPink      = Color(0xFFF472B6)
val AccentPinkLight = Color(0xFFFDF2F8)

val AccentAmber      = Color(0xFFF59E0B)
val AccentAmberLight = Color(0xFFFFFBEB)

val AccentIndigo      = Color(0xFF6366F1)
val AccentIndigoLight = Color(0xFFEEF2FF)

val AccentCyan      = Color(0xFF06B6D4)
val AccentCyanLight = Color(0xFFECFEFF)

val AccentBrown      = Color(0xFF92400E)
val AccentBrownLight = Color(0xFFFEF3C7)

// ── Neutral / Surface ─────────────────────────
val SurfaceLight     = Color(0xFFF8F9FA)   // App background (warm off-white)
val SurfaceGreenTint = Color(0xFFFDF1EB)   // Unread / highlighted bg (warm tint)
val CardWhite        = Color(0xFFFFFFFF)
val DividerColor     = Color(0xFFE8E8E8)
val BorderLight      = Color(0xFFD1D5DB)

// ── Text ──────────────────────────────────────
val TextPrimary   = Color(0xFF1C1C1E)   // near-black
val TextSecondary = Color(0xFF6B7280)   // neutral gray
val TextTertiary  = Color(0xFF9CA3AF)   // lighter gray (disabled)
val TextOnDark    = Color(0xFFFFFFFF)
val TextOnPrimary = Color(0xFFFFFFFF)

// ── Status ────────────────────────────────────
val StatusSuccess = Emerald600          // #059669
val StatusWarning = Color(0xFFF59E0B)   // amber-500
val StatusError   = Color(0xFFEF4444)   // red-500
val StatusInfo    = Color(0xFF3B82F6)   // blue-500
val StatusPending = OrangePrimary       // soft orange

// ── Dark Mode (warm navy, not pure black) ─────
val DarkSurface        = Color(0xFF13131F)   // deep warm navy
val DarkCard           = Color(0xFF1E1E2E)   // card: slightly lighter navy
val DarkBackground     = Color(0xFF0D0D18)   // darkest bg
val DarkGreenPrimary   = OrangeLight         // warm light orange on dark
val DarkTealAccent     = OrangeSurface       // soft orange tint on dark






















