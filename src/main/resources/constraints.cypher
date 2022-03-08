CREATE CONSTRAINT constraint_user_userId_unique ON ( user:User ) ASSERT (user.userId) IS UNIQUE
CREATE CONSTRAINT constraint_hashtag_name ON ( hashtag: Hashtag ) ASSERT (hashtag.name) IS UNIQUE
