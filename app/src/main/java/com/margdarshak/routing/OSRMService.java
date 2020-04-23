package com.margdarshak.routing;

import com.mapbox.api.directions.v5.DirectionsService;
import com.mapbox.api.directions.v5.models.DirectionsResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OSRMService extends DirectionsService {

    //@GET("route/v1/{user}/{profile}/{coordinates}")
    @GET("route/v1/{profile}/{coordinates}")
    Call<DirectionsResponse> getCall(
            @Header("User-Agent") String userAgent,
            @Path("profile") String profile,
            @Path("coordinates") String coordinates,
            //@Query("access_token") String accessToken,
            @Query("alternatives") Boolean alternatives,
            @Query("geometries") String geometries,
            @Query("overview") String overview,
            @Query("radiuses") String radiuses,
            @Query("steps") Boolean steps,
            @Query("bearings") String bearings,
            @Query("continue_straight") Boolean continueStraight,
            @Query("annotations") String annotations,
            @Query("language") String language,
            @Query("roundabout_exits") Boolean roundaboutExits,
            @Query("voice_instructions") Boolean voiceInstructions,
            @Query("banner_instructions") Boolean bannerInstructions,
            @Query("voice_units") String voiceUnits,
            @Query("exclude") String exclude,
            @Query("approaches") String approaches,
            @Query("waypoints") String waypointIndices,
            @Query("waypoint_names") String waypointNames,
            @Query("waypoint_targets") String waypointTargets,
            @Query("enable_refresh") Boolean enableRefresh,
            @Query("walking_speed") Double walkingSpeed,
            @Query("walkway_bias") Double walkwayBias,
            @Query("alley_bias") Double alleyBias
    );

    @FormUrlEncoded
    @POST("route/v1/{user}/{profile}")
    Call<DirectionsResponse> postCall(
            @Header("User-Agent") String userAgent,
            @Path("user") String user,
            @Path("profile") String profile,
            @Field("coordinates") String coordinates,
            //@Query("access_token") String accessToken,
            @Field("alternatives") Boolean alternatives,
            @Field("geometries") String geometries,
            @Field("overview") String overview,
            @Field("radiuses") String radiuses,
            @Field("steps") Boolean steps,
            @Field("bearings") String bearings,
            @Field("continue_straight") Boolean continueStraight,
            @Field("annotations") String annotations,
            @Field("language") String language,
            @Field("roundabout_exits") Boolean roundaboutExits,
            @Field("voice_instructions") Boolean voiceInstructions,
            @Field("banner_instructions") Boolean bannerInstructions,
            @Field("voice_units") String voiceUnits,
            @Field("exclude") String exclude,
            @Field("approaches") String approaches,
            @Field("waypoints") String waypointIndices,
            @Field("waypoint_names") String waypointNames,
            @Field("waypoint_targets") String waypointTargets,
            @Field("enable_refresh") Boolean enableRefresh,
            @Field("walking_speed") Double walkingSpeed,
            @Field("walkway_bias") Double walkwayBias,
            @Field("alley_bias") Double alleyBias
    );
}
