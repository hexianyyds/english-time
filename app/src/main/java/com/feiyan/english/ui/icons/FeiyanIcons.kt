package com.feiyan.english.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 手写线性图标（1.5px 描边、圆头圆角），与「白」的设计系统一致。
 * 不依赖 material-icons-extended，体积可控。
 */
object FeiyanIcons {

    private fun outlined(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply(block).build()

    private fun ImageVector.Builder.stroke(width: Float = 1.5f, block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )
    }

    private fun ImageVector.Builder.solid(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
        path(fill = SolidColor(Color.Black), pathBuilder = block)
    }

    val Search: ImageVector by lazy {
        outlined("Search") {
            stroke {
                moveTo(11f, 4f)
                curveToRelative(3.9f, 0f, 7f, 3.1f, 7f, 7f)
                curveToRelative(0f, 3.9f, -3.1f, 7f, -7f, 7f)
                curveToRelative(-3.9f, 0f, -7f, -3.1f, -7f, -7f)
                curveToRelative(0f, -3.9f, 3.1f, -7f, 7f, -7f)
                close()
                moveTo(16.3f, 16.3f)
                lineTo(20.5f, 20.5f)
            }
        }
    }

    val Mic: ImageVector by lazy {
        outlined("Mic") {
            stroke {
                moveTo(12f, 3.5f)
                curveToRelative(1.9f, 0f, 3.5f, 1.6f, 3.5f, 3.5f)
                lineTo(15.5f, 12f)
                curveToRelative(0f, 1.9f, -1.6f, 3.5f, -3.5f, 3.5f)
                curveToRelative(-1.9f, 0f, -3.5f, -1.6f, -3.5f, -3.5f)
                lineTo(8.5f, 7f)
                curveToRelative(0f, -1.9f, 1.6f, -3.5f, 3.5f, -3.5f)
                close()
                moveTo(5.5f, 11.5f)
                curveToRelative(0f, 3.6f, 2.9f, 6.5f, 6.5f, 6.5f)
                curveToRelative(3.6f, 0f, 6.5f, -2.9f, 6.5f, -6.5f)
                moveTo(12f, 18.2f)
                lineTo(12f, 21f)
            }
        }
    }

    val BookOpen: ImageVector by lazy {
        outlined("BookOpen") {
            stroke {
                moveTo(12f, 7f)
                curveToRelative(-1.8f, -1.5f, -4.3f, -2f, -7f, -2f)
                lineTo(5f, 18.6f)
                curveToRelative(2.7f, 0f, 5.2f, 0.5f, 7f, 2f)
                curveToRelative(1.8f, -1.5f, 4.3f, -2f, 7f, -2f)
                lineTo(19f, 5f)
                curveToRelative(-2.7f, 0f, -5.2f, 0.5f, -7f, 2f)
                close()
                moveTo(12f, 7f)
                lineTo(12f, 20.4f)
            }
        }
    }

    val Article: ImageVector by lazy {
        outlined("Article") {
            stroke {
                moveTo(6f, 3.5f)
                lineTo(14f, 3.5f)
                lineTo(18.5f, 8f)
                lineTo(18.5f, 20.5f)
                lineTo(6f, 20.5f)
                close()
                moveTo(14f, 3.5f)
                lineTo(14f, 8f)
                lineTo(18.5f, 8f)
                moveTo(8.9f, 12.6f)
                lineTo(15.6f, 12.6f)
                moveTo(8.9f, 16.2f)
                lineTo(15.6f, 16.2f)
            }
        }
    }

    val Person: ImageVector by lazy {
        outlined("Person") {
            stroke {
                moveTo(12f, 11.4f)
                curveToRelative(2.1f, 0f, 3.8f, -1.7f, 3.8f, -3.8f)
                curveToRelative(0f, -2.1f, -1.7f, -3.8f, -3.8f, -3.8f)
                curveToRelative(-2.1f, 0f, -3.8f, 1.7f, -3.8f, 3.8f)
                curveToRelative(0f, 2.1f, 1.7f, 3.8f, 3.8f, 3.8f)
                close()
                moveTo(4.8f, 20.5f)
                curveToRelative(0f, -3.6f, 3.3f, -6.1f, 7.2f, -6.1f)
                curveToRelative(3.9f, 0f, 7.2f, 2.5f, 7.2f, 6.1f)
            }
        }
    }

    val Play: ImageVector by lazy {
        outlined("Play") {
            solid {
                moveTo(8.8f, 5.2f)
                lineTo(19f, 12f)
                lineTo(8.8f, 18.8f)
                close()
            }
        }
    }

    val Pause: ImageVector by lazy {
        outlined("Pause") {
            solid {
                moveTo(8.2f, 5f)
                lineTo(11f, 5f)
                lineTo(11f, 19f)
                lineTo(8.2f, 19f)
                close()
                moveTo(13.2f, 5f)
                lineTo(16f, 5f)
                lineTo(16f, 19f)
                lineTo(13.2f, 19f)
                close()
            }
        }
    }

    val ArrowLeft: ImageVector by lazy {
        outlined("ArrowLeft") {
            stroke {
                moveTo(14.5f, 5.4f)
                lineTo(8f, 12f)
                lineTo(14.5f, 18.6f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        outlined("ChevronRight") {
            stroke {
                moveTo(9.8f, 5.4f)
                lineTo(16.3f, 12f)
                lineTo(9.8f, 18.6f)
            }
        }
    }

    val Clock: ImageVector by lazy {
        outlined("Clock") {
            stroke {
                moveTo(12f, 3.6f)
                curveToRelative(4.6f, 0f, 8.4f, 3.8f, 8.4f, 8.4f)
                curveToRelative(0f, 4.6f, -3.8f, 8.4f, -8.4f, 8.4f)
                curveToRelative(-4.6f, 0f, -8.4f, -3.8f, -8.4f, -8.4f)
                curveToRelative(0f, -4.6f, 3.8f, -8.4f, 8.4f, -8.4f)
                close()
                moveTo(12f, 7.6f)
                lineTo(12f, 12.2f)
                lineTo(15.4f, 14.2f)
            }
        }
    }

    val Close: ImageVector by lazy {
        outlined("Close") {
            stroke {
                moveTo(6.6f, 6.6f)
                lineTo(17.4f, 17.4f)
                moveTo(17.4f, 6.6f)
                lineTo(6.6f, 17.4f)
            }
        }
    }

    val Heart: ImageVector by lazy {
        outlined("Heart") {
            stroke {
                moveTo(12f, 19.6f)
                curveToRelative(-0.3f, 0f, -0.6f, -0.1f, -0.8f, -0.3f)
                curveToRelative(-3.7f, -3.2f, -7.4f, -6.2f, -7.4f, -10.2f)
                curveToRelative(0f, -2.5f, 2f, -4.5f, 4.4f, -4.5f)
                curveToRelative(1.5f, 0f, 3f, 0.8f, 3.8f, 2f)
                curveToRelative(0.8f, -1.2f, 2.3f, -2f, 3.8f, -2f)
                curveToRelative(2.4f, 0f, 4.4f, 2f, 4.4f, 4.5f)
                curveToRelative(0f, 4f, -3.7f, 7f, -7.4f, 10.2f)
                curveToRelative(-0.2f, 0.2f, -0.5f, 0.3f, -0.8f, 0.3f)
                close()
            }
        }
    }

    val Headphones: ImageVector by lazy {
        outlined("Headphones") {
            stroke {
                moveTo(4.6f, 15f)
                lineTo(4.6f, 12.4f)
                curveToRelative(0f, -4.1f, 3.3f, -7.4f, 7.4f, -7.4f)
                curveToRelative(4.1f, 0f, 7.4f, 3.3f, 7.4f, 7.4f)
                lineTo(19.4f, 15f)
                moveTo(4.6f, 14.4f)
                curveToRelative(1.3f, 0f, 2.4f, 1.1f, 2.4f, 2.4f)
                lineTo(7f, 18.6f)
                curveToRelative(0f, 1.3f, -1.1f, 2.4f, -2.4f, 2.4f)
                close()
                moveTo(19.4f, 14.4f)
                curveToRelative(-1.3f, 0f, -2.4f, 1.1f, -2.4f, 2.4f)
                lineTo(17f, 18.6f)
                curveToRelative(0f, 1.3f, 1.1f, 2.4f, 2.4f, 2.4f)
                close()
            }
        }
    }

    val Bookmark: ImageVector by lazy {
        outlined("Bookmark") {
            stroke {
                moveTo(7f, 4.5f)
                lineTo(17f, 4.5f)
                lineTo(17f, 20f)
                lineTo(12f, 16.2f)
                lineTo(7f, 20f)
                close()
            }
        }
    }
}
