-- query for single star
select s.name, s.birthYear, m.title
From stars s
JOIN stars_in_movies sim ON s.id=sim.starId
JOIN movies m ON sim.movieId=m.id
Where s.id=?;

-- query for single movie
SELECT gim.movieId, m.title, m.year, m.director,
       SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name ASC), ',', LENGTH(GROUP_CONCAT(DISTINCT s.name))) AS star,
       SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name ASC), ',', LENGTH(GROUP_CONCAT(DISTINCT s.name))) AS star,
       SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name), ',', LENGTH(GROUP_CONCAT(DISTINCT g.name))) AS genre,
       r.rating
FROM movies m
JOIN stars_in_movies sim ON m.id=sim.movieId
JOIN stars s ON sim.starId=s.id
JOIN genres_in_movies gim on m.id = gim.movieId
JOIN genres g on gim.genreId = g.id
JOIN ratings r ON r.movieId=m.id
Where m.id=?;

-- query for movies list
SELECT m.id AS movieId, m.title, m.year, m.director,
    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ','), ',', 3) AS genres,
    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY mdb.num_movies DESC, s.name ASC SEPARATOR ','), ',', 3) AS stars,
    SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY mdb.num_movies DESC, s.name ASC SEPARATOR ','), ',', 3) AS stars_id,
    ROUND(AVG(r.rating), 2) AS rating
FROM
    movies AS m
JOIN genres_in_movies AS gim ON m.id = gim.movieId
JOIN genres AS g ON g.id = gim.genreId
JOIN stars_in_movies AS sim ON sim.movieId = m.id
JOIN stars AS s ON s.id = sim.starId
LEFT JOIN ratings AS r ON r.movieId = m.id
INNER JOIN (
    SELECT sim.starId, COUNT(DISTINCT sim.movieId) AS num_movies
    FROM stars_in_movies AS sim
    GROUP BY sim.starId) AS mdb ON s.id = mdb.starId
JOIN (
    SELECT movieId, AVG(rating) AS avg_rating
    FROM ratings
    GROUP BY movieId
    ORDER BY avg_rating DESC
    LIMIT 20) AS top_movies ON m.id = top_movies.movieId
GROUP BY m.id, m.title, m.year, m.director
ORDER BY rating DESC;

ALTER TABLE movies ADD COLUMN price INT;
SET SQL_SAFE_UPDATES = 0;
UPDATE movies SET price = FLOOR(1 + (RAND() * 100)) WHERE id IS NOT NULL;
SET SQL_SAFE_UPDATES = 1;