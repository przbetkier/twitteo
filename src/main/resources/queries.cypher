// Users not followed by me but followed by people I do follow. Ordered by popularity

MATCH (u: User {displayName: "<user>"})-[:FOLLOWS*2]->(f:User)
WHERE NOT (u)-[:FOLLOWS]->(f) AND u <> f
WITH f
MATCH (f)<-[fol: FOLLOWS]-(:User)
RETURN f, count(fol) as followers
ORDER BY followers DESC
LIMIT 10

// Posts I do not like yet but liked by people I follow posted by people I do not follow.

MATCH (u: User {displayName: "<user>"})-[fr:FOLLOWS]->(fol:User)-[lr:LIKES]->(t:Tweet)<-[pr:POSTS]-(op: User)
WHERE NOT (u)-[:FOLLOWS]->(op) AND u <> op AND NOT (u)-[:LIKES]->(t)
RETURN DISTINCT t ORDER BY t.createdAt DESC
LIMIT 10


// TRENDING THIS WEEK

// Most liked tweets
MATCH (t:Tweet)<-[r:LIKES]-()
WHERE t.createdAt > datetime() - duration('P7D')
WITH t as tweet, count(r) as likes
RETURN ID(tweet), tweet.createdAt, likes ORDER BY likes DESC
LIMIT 10

// Most replied tweets
MATCH (t:Tweet)<-[r:REPLIES_TO]-(:Tweet)
WHERE datetime(t.createdAt) > datetime() - duration('P7D')
WITH t as tweet, count(r) as replies
RETURN ID(tweet), tweet.createdAt, replies ORDER BY replies DESC
LIMIT 10

//OR Most discussed tweet
MATCH p=(t:Tweet)<-[rt:REPLIES_TO*..50]-(:Tweet)
WHERE NOT t:Reply AND t.createdAt > datetime() - duration('P7D')
RETURN ID(t), COUNT(distinct rt) as comments
ORDER BY comments DESC
LIMIT 10

// Most popular hashtags
MATCH (h:Hashtag)<-[tags:TAGS]-(t:Tweet)
WHERE t.createdAt > datetime() - duration('P7D')
RETURN h.name, COUNT(tags) as posts
ORDER BY posts DESC
LIMIT 10

// Most followed users
MATCH (u:User)<-[follow:FOLLOWS]-(:User)
WHERE follow.since > datetime() - duration("P7D")
RETURN u.displayName, count(follow) as follows
ORDER BY follows DESC
LIMIT 10


