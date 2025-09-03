package no.uio.ifi.in2000.team33.lumo.data.frost.model.observation

data class Observation(
    val elementId: String,
    val exposureCategory: String,
    val level: Level,
    val performanceCategory: String,
    val qualityCode: Int,
    val timeOffset: String,
    val timeResolution: String,
    val timeSeriesId: Int,
    val unit: String,
    val value: Double
)