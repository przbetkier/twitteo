package dev.przbetkier.twitteo.domain.user

interface UserRepositoryCustom {

    fun getUserData(userId: String): UserResponse
    fun getFollowers(userId: String): FollowerResponse
    fun getFollowerState(followerUid: String, followeeUid: String): FollowerState
    fun follow(followerUid: String, followeeUid: String): FollowerState
    fun unfollow(followerUid: String, followeeUid: String): FollowerState
    fun setBio(userId: String, bio: String)
    fun searchUserWithDisplayName(query: String, limit: Long = 5): List<UserResponse>
}
