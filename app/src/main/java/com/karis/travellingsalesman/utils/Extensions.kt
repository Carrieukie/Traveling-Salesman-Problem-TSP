package com.karis.travellingsalesman.utils

fun convertSecondsToTime(seconds: Long): String {
    var remainingSeconds = seconds

    val years = remainingSeconds / (60 * 60 * 24 * 365)
    remainingSeconds %= 60 * 60 * 24 * 365

    val months = remainingSeconds / (60 * 60 * 24 * 30)
    remainingSeconds %= 60 * 60 * 24 * 30

    val weeks = remainingSeconds / (60 * 60 * 24 * 7)
    remainingSeconds %= 60 * 60 * 24 * 7

    val days = remainingSeconds / (60 * 60 * 24)
    remainingSeconds %= 60 * 60 * 24

    val hours = remainingSeconds / (60 * 60)
    remainingSeconds %= 60 * 60

    val minutes = remainingSeconds / 60
    remainingSeconds %= 60

    return buildString {
        append("(")
        if (years > 0) append("$years yrs ")
        if (months > 0) append("$months mths ")
        if (weeks > 0) append("$weeks wks ")
        if (days > 0) append("$days dys ")
        if (hours > 0) append("$hours hr ")
        if (minutes > 0) append("$minutes min")
        append(")")
    }
}
