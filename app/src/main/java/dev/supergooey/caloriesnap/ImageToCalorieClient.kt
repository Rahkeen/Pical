package dev.supergooey.caloriesnap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private const val BASE_URL = "https://api.anthropic.com/v1/"
private const val API_KEY = BuildConfig.API_KEY
private const val VERSION = "2023-06-01"

@Serializable
data class MessagesRequest(
  val model: String = "claude-3-5-sonnet-20240620",
  val system: String = """
    You are an "Image-to-Calorie" engine. Process the input image, complete these four tasks in order. You response must only be a single JSON object.
    1 - Figure if the image contains a food or not. Based on this, set the field "valid" to either true or false in the JSON object and skip next steps if not.
    2 - Analyze the food in the image and estimate its total calories. Set this value to "total_calories" field in the JSON object.
    3 - Describe the food in the image with a snarky attitude, but keep it concise. If you see something healthy, keep the same snarky attitude but also praise the user a little bit for making a good choice. Set this description to "food_description" field in the JSON object.
    4 - Create a short 2-4 word title for the food in the image. Be accurate, but have some fun with it! Set this value to "food_title" field in the JSON object.
  """.trimIndent(),
  @SerialName("max_tokens") val maxTokens: Int = 2048,
  val messages: List<Message>
)

@Serializable
data class MessagesResponse(
  val id: String,
  val model: String,
  val role: String,
  val type: String,
  @SerialName("stop_reason") val stopReason: String,
  @SerialName("stop_sequence") val stopSequence: List<String>?,
  val usage: Usage,
  val content: List<MessageContent>
)

@Serializable
data class Usage(
  @SerialName("input_tokens") val inputTokens: Int,
  @SerialName("output_tokens") val outputTokens: Int
)

@Serializable
data class Message(
  val role: String,
  val content: List<MessageContent>,
)

@Serializable
sealed class MessageContent {
  @Serializable
  @SerialName("text")
  data class Text(val text: String): MessageContent()

  @Serializable
  @SerialName("image")
  data class Image(val source: ImageSource): MessageContent()
}

@Serializable
data class ImageSource(
  val type: String = "base64",
  @SerialName("media_type") val mediaType: String = "image/jpeg",
  val data: String
)

interface ClaudeService {

  @POST("messages")
  suspend fun getMessages(@Body request: MessagesRequest): retrofit2.Response<MessagesResponse>
}

object ImageToCalorieClient {
  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }
  private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HeaderInterceptor())
    .build()

  private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
    .build()

  val api: ClaudeService = retrofit.create(ClaudeService::class.java)
}

internal class HeaderInterceptor: Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val modifiedRequest = chain.request()
      .newBuilder()
      .header("content-type", "application/json")
      .addHeader("x-api-key", API_KEY)
      .addHeader("anthropic-version", VERSION)
      .build()

    return chain.proceed(modifiedRequest)
  }
}