package com.exyui.thor.core.model

import com.exyui.thor.core.database.Comment
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import rx.Observable

/**
 * Created by yuriel on 1/24/17.
 */
class CommentTypeAdapter: TypeAdapter<Comment>() {
    override fun write(writer: JsonWriter, c: Comment) {
        writer.beginObject()
        Comment.publicFields.forEach {
            val v = writer.name(it)
            v.apply {
                when(it) {
                    "id" -> value(c.id)
                    "parent" -> value(c.parent)
                    "created" -> value(c.created)
                    "modified" -> value(c.modified)
                    "text" -> value(c.text)
                    "author" -> value(c.author)
                    "website" -> value(c.website)
                    "likes" -> value(c.likes)
                    "dislikes" -> value(c.dislikes)
                    "userIdentity" -> value(c.userIdentity)
                    "totalReplies" -> value(c.totalReplies)
                    "hiddenReplies" -> value(c.hiddenReplies)
                    "replies" -> {
                        writer.beginArray()
                        c.replies.forEach {
                            write(writer, it)
                        }
                        writer.endArray()
                    }
                }
            }
        }
        writer.endObject()
    }

    override fun read(reader: JsonReader): Comment {
        return Observable
                .create<JsonReader> {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        try {
                            it.onNext(reader)
                        } catch (e: Exception) {
                            it.onError(e)
                        }
                    }
                    reader.endObject()
                    it.onCompleted()
                }
                .reduce(Comment.ApiBuilder()) { builder, reader ->
                    when(reader.nextName()) {
                        "id" -> builder.id = reader.nextInt()
                        "parent" -> builder.parent = reader.nextInt()
                        "created" -> builder.created = reader.nextDouble()
                        "modified" -> builder.modified = reader.nextDouble()
                        "text" -> builder.text = reader.nextString()
                        "author" -> builder.author = reader.nextString()
                        "website" -> builder.website = reader.nextString()
                        "likes" -> builder.likes = reader.nextInt()
                        "dislikes" -> builder.dislikes = reader.nextInt()
                        "userIdentity" -> builder.userIdentity = reader.nextString()
                        "totalReplies" -> builder.totalReplies = reader.nextInt()
                        "hiddenReplies" -> builder.hiddenReplies = reader.nextInt()
                        "replies" -> {
                            reader.beginArray()
                            while(reader.hasNext()) {
                                val c = read(reader)
                                builder.replies.add(c)
                            }
                            reader.endArray()
                        }
                    }
                    builder
                }
                .toBlocking()
                .single()
                .build()
    }
}