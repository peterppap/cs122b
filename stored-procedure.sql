USE moviedb;

DELIMITER //

CREATE PROCEDURE add_movie(IN movieTitle VARCHAR(100),
                           IN movieYear INT,
                           IN movieDirector VARCHAR(100),
                           IN starName VARCHAR(100),
                           IN starYear INT,
                           IN genreName VARCHAR(32),
                           OUT getMovieId VARCHAR(10),
                           OUT getStarId VARCHAR(10),
                           OUT getGenreId INT)

BEGIN
    DECLARE maxMovieId, newMovieId VARCHAR(10);
    DECLARE maxStarId, newStarId VARCHAR(10);
    DECLARE maxGenreId, newGenreId INT;
    DECLARE findMovieId VARCHAR(10);
    DECLARE findStarId, findGenreId VARCHAR(10);
    DECLARE prefix VARCHAR(10);
    DECLARE number_part INT;
    DECLARE new_number INT;

    -- Check if movie already exists
    SELECT id INTO findMovieId FROM movies WHERE title = movieTitle AND year = movieYear AND director = movieDirector;
    IF findMovieId IS NOT NULL THEN
        SET getMovieId = '';
        SET getStarId = '';
        SET getGenreId = -1;
    ELSE
        -- Get the max movie Id and create a new movie id
        SELECT MAX(id) INTO maxMovieId FROM movies;
        SET prefix = SUBSTRING(maxMovieId, 1, LENGTH(maxMovieId) - LENGTH(SUBSTRING_INDEX(maxMovieId, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(maxMovieId, '9', ''), '8', ''), '7', ''), '6', ''), '5', ''), '4', ''), '3', ''), '2', ''), '1', ''), '0', ''), -1)));
        SET number_part = CAST(SUBSTRING(maxMovieId, LENGTH(prefix) + 1) AS UNSIGNED);
        SET new_number = number_part + 1;
        SET newMovieId = CONCAT(prefix, LPAD(new_number, LENGTH(SUBSTRING(maxMovieId, LENGTH(prefix) + 1)), '0'));


        -- Check if star already exists
        SELECT id INTO findStarId FROM stars WHERE name = starName;
        IF findStarId IS NULL THEN
            -- Get the max star Id and create a new star id
            SELECT MAX(id) INTO maxStarId FROM stars;
            SET prefix = SUBSTRING(maxStarId, 1, LENGTH(maxStarId) - LENGTH(SUBSTRING_INDEX(maxStarId, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(maxStarId, '9', ''), '8', ''), '7', ''), '6', ''), '5', ''), '4', ''), '3', ''), '2', ''), '1', ''), '0', ''), -1)));
            SET number_part = CAST(SUBSTRING(maxStarId, LENGTH(prefix) + 1) AS UNSIGNED);
            SET new_number = number_part + 1;
            SET newStarId = CONCAT(prefix, LPAD(new_number, LENGTH(SUBSTRING(maxStarId, LENGTH(prefix) + 1)), '0'));

            INSERT INTO stars (id, name, birthYear) VALUES (newStarId, starName, starYear);
            SET findStarId = newStarId;
        END IF;

        -- Check if genre already exists
        SELECT id INTO findGenreId FROM genres WHERE name = genreName;
        IF findGenreId IS NULL THEN
            -- Get the max genre Id and create a new genre id
            SELECT MAX(id) INTO maxGenreId FROM genres;
            SET newGenreId = maxGenreId + 1;
            INSERT INTO genres (id, name) VALUES (newGenreId, genreName);
            SET findGenreId = newGenreId;
        END IF;

        INSERT INTO movies (id, title, year, director) VALUES (newMovieId, movieTitle, movieYear, movieDirector);
        INSERT INTO stars_in_movies (starId, movieId) VALUES (findStarId, newMovieId);
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (findGenreId, newMovieId);
        INSERT INTO ratings (movieId, rating, numVotes) VALUES (
                   newMovieId,
                   ROUND(RAND() * 10, 1),  -- Generates a random float between 0 and 10, rounded to 1 decimal place
                   FLOOR(RAND() * 101)     -- Generates a random integer between 0 and 100
               );

        -- Get the return parameters
        SET getMovieId = newMovieId;
        SET getStarId = findStarId;
        SET getGenreId = findGenreId;
    END IF;
END //

DELIMITER ;

--
-- DELIMITER //
--
-- CREATE PROCEDURE check_movies(IN movieTitle VARCHAR(100),
--                               IN movieYear INT,
--                               IN movieDirector VARCHAR(100),OUT getId VARCHAR(10))
--
-- BEGIN
--     DECLARE maxMovieId, newMovieId VARCHAR(10);
--     DECLARE prefix VARCHAR(10);
--     DECLARE number_part INT;
--     DECLARE new_number INT;
--     DECLARE findMovieId VARCHAR(10);
--
--     SELECT id INTO findGenreId FROM genres WHERE name = genreName;
--     IF findGenreId IS NULL THEN
--         SELECT MAX(id) INTO maxGenreId FROM genres;
--
--         SET new_number = number_part + 1;
--
--         INSERT INTO genres (id, name) VALUES (newGenreId, genreName);
--         SET findGenreId = newGenreId;
--     END IF;
-- END //
-- DELIMITER ;
--
--
-- DELIMITER //
--
-- CREATE PROCEDURE check_Star(IN starName VARCHAR(100),
--                             IN starYear INT,
--                             OUT getId VARCHAR(10))
--
-- BEGIN
--     DECLARE maxStarId, newStarId VARCHAR(10);
--     DECLARE prefix VARCHAR(10);
--     DECLARE number_part INT;
--     DECLARE new_number INT;
--
--     SELECT MAX(id) INTO maxStarId FROM stars;
--     SET prefix = SUBSTRING(maxStarId, 1, LENGTH(maxStarId) - LENGTH(SUBSTRING_INDEX(maxStarId, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(maxStarId, '9', ''), '8', ''), '7', ''), '6', ''), '5', ''), '4', ''), '3', ''), '2', ''), '1', ''), '0', ''), -1)));
--     SET number_part = CAST(SUBSTRING(maxStarId, LENGTH(prefix) + 1) AS UNSIGNED);
--     SET new_number = number_part + 1;
--     SET newStarId = CONCAT(prefix, LPAD(new_number, LENGTH(SUBSTRING(maxStarId, LENGTH(prefix) + 1)), '0'));
--     INSERT INTO stars (id, name, birthYear) VALUES (newStarId, starName, starYear);
--     SET getId = newStarId;
-- END //
-- DELIMITER ;