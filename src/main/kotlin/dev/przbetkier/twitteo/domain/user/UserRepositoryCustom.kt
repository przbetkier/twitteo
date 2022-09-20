package dev.przbetkier.twitteo.domain.user

interface UserRepositoryCustom {

    fun getUserData(userId: String): UserResponse
    fun getUserDataByDisplayName(displayName: String): UserResponse
    fun getFollowerState(followerUid: String, followeeUid: String): FollowerState
    fun follow(followerUid: String, followeeUid: String): FollowerState
    fun unfollow(followerUid: String, followeeUid: String): FollowerState
    fun setBio(userId: String, bio: String)
    fun searchUserWithDisplayName(query: String, limit: Long = 5): List<UserResponse>
}
