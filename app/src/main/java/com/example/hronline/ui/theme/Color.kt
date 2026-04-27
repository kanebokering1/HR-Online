package com.example.hronline.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════
// HROES v2 — Emerald Green Color System (Flat)
// ═══════════════════════════════════════════

// ── Emerald Primary Palette ──────────────────
val Emerald50  = Color(0xFFECFDF5)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald200 = Color(0xFFA7F3D0)
val Emerald300 = Color(0xFF6EE7B7)
val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981)   // Main brand — light contexts
val Emerald600 = Color(0xFF059669)   // Primary action buttons, headers
val Emerald700 = Color(0xFF047857)   // Pressed / dark variant
val Emerald800 = Color(0xFF065F46)   // Dark header
val Emerald900 = Color(0xFF064E3B)   // Darkest

// Aliases for backward compatibility with existing code
val GreenPrimary  = Emerald600          // #059669
val GreenLight    = Emerald400          // #34D399
val GreenDark     = Emerald800          // #065F46
val TealAccent    = Emerald500          // #10B981 (same family, no teal gradient)
val TealLight     = Emerald300
val TealDark      = Emerald700
// Legacy shade aliases (map to emerald equivalents)
val Green50       = Emerald50           // #ECFDF5
val Green100      = Emerald100          // #D1FAE5

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
val SurfaceLight     = Color(0xFFF8FAFC)   // App background (off-white, not green tint)
val SurfaceGreenTint = Color(0xFFECFDF5)   // Unread / highlighted bg
val CardWhite        = Color(0xFFFFFFFF)
val DividerColor     = Color(0xFFE2E8F0)
val BorderLight      = Color(0xFFCBD5E1)

// ── Text ──────────────────────────────────────
val TextPrimary   = Color(0xFF0F172A)   // slate-900 — sharp, modern
val TextSecondary = Color(0xFF64748B)   // slate-500
val TextTertiary  = Color(0xFF94A3B8)   // slate-400 (disabled)
val TextOnDark    = Color(0xFFFFFFFF)
val TextOnPrimary = Color(0xFFFFFFFF)

// ── Status ────────────────────────────────────
val StatusSuccess = Emerald600          // #059669
val StatusWarning = Color(0xFFF59E0B)   // amber-500
val StatusError   = Color(0xFFEF4444)   // red-500
val StatusInfo    = Color(0xFF3B82F6)   // blue-500
val StatusPending = Color(0xFFF97316)   // orange-500

// ── Dark Mode ─────────────────────────────────
val DarkSurface        = Color(0xFF0F172A)
val DarkCard           = Color(0xFF1E293B)
val DarkBackground     = Color(0xFF020617)
val DarkGreenPrimary   = Emerald400          // #34D399 (bright on dark)
val DarkTealAccent     = Emerald300          // #6EE7B7
