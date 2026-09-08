package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.WorksheetDocument
import com.example.model.WorksheetLine
import com.example.model.WorksheetLineType
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Entity(tableName = "worksheet_documents")
data class WorksheetEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val linesJson: String,
    val grandTotal: Double
) {
    fun toDocument(): WorksheetDocument {
        val lineList = mutableListOf<WorksheetLine>()
        var extractedHeader = ""
        try {
            val jsonTrimmed = linesJson.trim()
            val jsonArray = if (jsonTrimmed.startsWith("{")) {
                val rootObj = JSONObject(jsonTrimmed)
                extractedHeader = rootObj.optString("headerNote", "")
                rootObj.optJSONArray("lines") ?: JSONArray()
            } else {
                JSONArray(jsonTrimmed)
            }

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lineList.add(
                    WorksheetLine(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        lineType = try {
                            WorksheetLineType.valueOf(obj.optString("lineType", WorksheetLineType.CALCULATION.name))
                        } catch (e: Exception) {
                            WorksheetLineType.CALCULATION
                        },
                        operator = obj.optString("operator", "+"),
                        rawValue = obj.optString("rawValue", "0"),
                        evaluatedNumber = obj.optDouble("evaluatedNumber", 0.0),
                        percentageDelta = if (obj.has("percentageDelta") && !obj.isNull("percentageDelta")) obj.getDouble("percentageDelta") else null,
                        runningTotal = obj.optDouble("runningTotal", 0.0),
                        variableName = if (obj.has("variableName") && !obj.isNull("variableName")) obj.getString("variableName") else null,
                        note = obj.optString("note", ""),
                        hasDividerBefore = obj.optBoolean("hasDividerBefore", false)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback empty
        }
        return WorksheetDocument(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lines = lineList,
            grandTotal = grandTotal,
            headerNote = extractedHeader
        )
    }

    companion object {
        fun fromDocument(doc: WorksheetDocument): WorksheetEntity {
            val jsonArray = JSONArray()
            doc.lines.forEach { line ->
                val obj = JSONObject().apply {
                    put("id", line.id)
                    put("lineType", line.lineType.name)
                    put("operator", line.operator)
                    put("rawValue", line.rawValue)
                    put("evaluatedNumber", line.evaluatedNumber)
                    if (line.percentageDelta != null) put("percentageDelta", line.percentageDelta)
                    put("runningTotal", line.runningTotal)
                    if (line.variableName != null) put("variableName", line.variableName)
                    put("note", line.note)
                    put("hasDividerBefore", line.hasDividerBefore)
                }
                jsonArray.put(obj)
            }
            val rootObj = JSONObject().apply {
                put("headerNote", doc.headerNote)
                put("lines", jsonArray)
            }
            return WorksheetEntity(
                id = doc.id,
                title = doc.title,
                createdAt = doc.createdAt,
                updatedAt = doc.updatedAt,
                linesJson = rootObj.toString(),
                grandTotal = doc.grandTotal
            )
        }
    }
}
