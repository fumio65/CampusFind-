package com.campusfind.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusfind.ui.theme.ModernAccent
import com.campusfind.ui.theme.ModernFound
import com.campusfind.ui.theme.ModernLost
import kotlinx.coroutines.launch

private data class OnboardingData(
    val emoji: String,
    val title: String,
    val highlight: String,      // coloured word inside the title
    val description: String,
    val accentColor: Color,
    val gradientColors: List<Color>
)

private val pages = listOf(
    OnboardingData(
        emoji        = "🎓",
        title        = "Welcome to",
        highlight    = "CampusFind+",
        description  = "The campus lost & found platform built for students. Reunite people with their belongings — fast.",
        accentColor  = ModernAccent,
        gradientColors = listOf(Color(0xFF0E0C1A), Color(0xFF1A1340), Color(0xFF0C1A14))
    ),
    OnboardingData(
        emoji        = "🔍",
        title        = "Report &",
        highlight    = "Discover",
        description  = "Snap a photo, add a location, and post in seconds. Browse what others have found on campus right now.",
        accentColor  = Color(0xFF7C6FFF),
        gradientColors = listOf(Color(0xFF0C0C1E), Color(0xFF1C1450), Color(0xFF0A1810))
    ),
    OnboardingData(
        emoji        = "🤝",
        title        = "Trusted",
        highlight    = "Community",
        description  = "Verified campus accounts only. Submit claims, leave tips, and get notified the moment your item is found.",
        accentColor  = ModernFound,
        gradientColors = listOf(Color(0xFF081410), Color(0xFF0E2820), Color(0xFF101030))
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()
    val page       = pages[pagerState.currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(colors = page.gradientColors)
            )
    ) {
        // ── Ambient glow orbs ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 180.dp, y = (-60).dp)
                .background(page.accentColor.copy(alpha = 0.12f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-40).dp, y = 500.dp)
                .background(page.accentColor.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 250.dp, y = 560.dp)
                .background(ModernFound.copy(alpha = 0.07f), CircleShape)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // ── Skip ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick   = onComplete,
                        modifier  = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            "Skip",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color      = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // ── Pager ──────────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                PageContent(data = pages[index])
            }

            // ── Dots ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 7.dp,
                        animationSpec = tween(300),
                        label       = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(7.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) page.accentColor
                                else Color.White.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            // ── CTA button ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
            ) {
                val isLast = pagerState.currentPage == pages.size - 1
                Button(
                    onClick = {
                        if (!isLast) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation    = 20.dp,
                            shape        = RoundedCornerShape(16.dp),
                            ambientColor = page.accentColor.copy(0.5f),
                            spotColor    = page.accentColor.copy(0.5f)
                        ),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(page.accentColor, page.accentColor.copy(0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = if (isLast) "Get Started" else "Next",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun PageContent(data: OnboardingData) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Emoji in glowing ring ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(
                    elevation    = 32.dp,
                    shape        = CircleShape,
                    ambientColor = data.accentColor.copy(0.6f),
                    spotColor    = data.accentColor.copy(0.5f)
                )
                .background(
                    Brush.linearGradient(
                        listOf(data.accentColor.copy(0.25f), data.accentColor.copy(0.10f))
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Outer ring
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            )
            Text(data.emoji, fontSize = 56.sp)
        }

        Spacer(Modifier.height(48.dp))

        // ── Title (plain + coloured highlight on second line) ──────────────
        Text(
            text       = data.title,
            fontSize   = 32.sp,
            fontWeight = FontWeight.Black,
            color      = Color.White,
            textAlign  = TextAlign.Center,
            style      = LocalTextStyle.current.copy(
                shadow = Shadow(Color.Black.copy(0.4f), Offset(0f, 2f), 8f)
            )
        )
        Text(
            text       = data.highlight,
            fontSize   = 32.sp,
            fontWeight = FontWeight.Black,
            color      = data.accentColor,
            textAlign  = TextAlign.Center,
            style      = LocalTextStyle.current.copy(
                shadow = Shadow(data.accentColor.copy(0.4f), Offset(0f, 2f), 16f)
            )
        )

        Spacer(Modifier.height(20.dp))

        // ── Description ────────────────────────────────────────────────────
        Text(
            text       = data.description,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Normal,
            color      = Color.White.copy(alpha = 0.65f),
            textAlign  = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}
