CREATE CONSTRAINT constraint_user_userId_unique ON ( user:User ) ASSERT (user.userId) IS UNIQUE
CREATE CONSTRAINT constraint_hashtag_name ON ( hashtag: Hashtag ) ASSERT (hashtag.name) IS UNIQUE

CREATE CONSTRAINT constraint_bankaccount_id_unique ON ( bankaccount:BankAccount ) ASSERT (bankaccount.id) IS UNIQUE
CREATE CONSTRAINT constraint_bankaccount_value_unique ON ( bankaccount:BankAccount ) ASSERT (bankaccount.value) IS UNIQUE

CREATE INDEX index_cookie_last_used_at FOR (cookie: Cookie) ON (cookie.lastUsedAt)
CREATE INDEX user_wanted FOR (user: User) ON (user.wanted)
CREATE INDEX user_blocked_sell FOR (user: User) ON (user.blockedSell)
