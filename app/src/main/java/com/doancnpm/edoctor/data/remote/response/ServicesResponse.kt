package com.doancnpm.edoctor.data.remote.response

import com.squareup.moshi.Json

data class ServicesResponse(
  @Json(name = "pagination")
  val pagination: Pagination,
  @Json(name = "services")
  val services: List<Service>
) {
  data class Service(
    @Json(name = "description")
    val description: String, // Sapiente quia voluptatem illum optio in enim quae veniam dolor est tempore repellat aperiam sint et ad ullam sit quia mollitia voluptates eos doloremque dicta ab explicabo vel dolores incidunt rem et debitis in ut qui consequuntur inventore similique atque qui neque ut sed cupiditate esse asperiores quis sequi excepturi qui odit ut porro rerum et quam dolores ut aperiam placeat non vero est ipsa illum et eum cumque ipsa consequatur nesciunt assumenda minima sed.
    @Json(name = "id")
    val id: Long, // 6
    @Json(name = "image")
    val image: Image,
    @Json(name = "name")
    val name: String, // Hollie
    @Json(name = "price")
    val price: Int // 6757000
  )
}