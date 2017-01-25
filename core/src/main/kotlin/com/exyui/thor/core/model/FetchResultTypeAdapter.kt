package com.exyui.thor.core.model

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import rx.Observable

/**
 * Created by yuriel on 1/25/17.
 */
object FetchResultTypeAdapter: TypeAdapter<FetchResult>() {
    private val fields = listOf("id", "totalReplies", "hiddenReplies", "replies")
    override fun write(writer: JsonWriter, f: FetchResult) {
        writer.beginObject()
        fields.forEach {
            val v = writer.name(it)
            v.apply {
                when(it) {
                    "id" -> value(f.id)
                    "totalReplies" -> value(f.totalReplies)
                    "hiddenReplies" -> value(f.hiddenReplies)
                    "replies" -> {
                        writer.beginArray()
                        f.replies.forEach {
                            CommentTypeAdapter.write(writer, it)
                        }
                        writer.endArray()
                    }
                }
            }
        }
        writer.endObject()
    }

    override fun read(reader: JsonReader): FetchResult {
        return Observable
                .create<JsonReader> {
                    it.onStart()
                    reader.beginObject()
                    while (reader.hasNext()) {
                        try {
                            it.onNext(reader)
                        } catch (e: Exception) {
                            it.onError(e)
                        }
                    }
                    reader.endArray()
                    it.onCompleted()
                }
                .reduce(FetchResult()) { result, reader ->
                    when(reader.nextName()) {
                        "id" -> result.id = reader.nextInt()
                        "totalReplies" -> result.totalReplies = reader.nextInt()
                        "hiddenReplies" -> result.hiddenReplies = reader.nextInt()
                        "replies" -> {
                            reader.beginArray()
                            while(reader.hasNext()) {
                                val c = CommentTypeAdapter.read(reader)
                                result.replies.add(c)
                            }
                            reader.endArray()
                        }
                    }
                    result
                }
                .toBlocking()
                .single()
    }
}