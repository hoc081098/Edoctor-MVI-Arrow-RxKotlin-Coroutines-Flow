package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class CategoriesResponse(
  @Json(name = "categories")
  val categories: List<Category>,
  @Json(name = "pagination")
  val pagination: Pagination,
) {
  data class Category(
    @Json(name = "description")
    val description: String, // Voluptates dolor pariatur vero fuga quidem suscipit illo maxime quisquam nisi aut omnis consequatur aut et omnis rerum est molestias deleniti fugit quos est ullam minus nobis sint fuga voluptatem aperiam est dicta corporis voluptatem in consequatur magnam repellendus dolor omnis optio voluptate et et earum non fugiat neque repudiandae perspiciatis aut nesciunt et.
    @Json(name = "id")
    val id: Int, // 5
    @Json(name = "image")
    val image: Image,
    @Json(name = "name")
    val name: String, // Ariel
  ) {
    data class Image(
      @Json(name = "extension")
      val extension: String, // jpg
      @Json(name = "id")
      val id: Int, // 40
      @Json(name = "url")
      val url: String, // system/categories/category.jpg
    )
  }

  data class Pagination(
    @Json(name = "page")
    val page: Int, // 2
    @Json(name = "per_page")
    val perPage: Int, // 4
    @Json(name = "total_page")
    val totalPage: Int, // 2
  )
}