# Team Termination
# CS 122B Project 3
Using prepareStatement files: 
- AddStar.java
- CartServlet.java
- ConfirmServlet.java
- LoginServlet.java
- MoviesServlet.java
- PaymentServlet.java
- ResultServlet.java
- SearchServlet.java
- SingleMovieServlet.java
- SingleStarServlet.java

2 parsing time optimization strategies:
- We apply batch insertion, executing multiple insertions in a single database transaction. This strategy reduces the attempts of establishing multiple database connections and improves efficiency by minimizing the connection between the application and the database.
- The time complexity of the algorithm is O(1) for each comparison and insertion. In particularly, we use hashmap to cache sql data, achieving O(1) constant time to check duplicate insertions.


Inconsistency data:
InconsistentMovies.txt -- Defined as movies that missing primary key or title or director or year 
DuplicateMovies.txt -- Defined as movies that duplicate during parsing/insertion
DuplicateStars.txt -- Defined as stars that duplicate during parsing/insertion

Zhentao Yang:
* Task 1, Task 2, Task 6
* Video Recording

David Liu:
* Task 3, Task 4, Task 5
* Debug for project 2 search pagination and prev/next button


# Video URL:
Video was corrcupted when it was generated. Need to resubmit the video.


# CS 122B Project 2
Zhentao Yang:
* Debug genres sorting and hyperlinked

* Generate random price for each movie in database

* Extend Project 1

David Liu:
* Code Backend Page (login, search, result, cart, confirmation)

* Beautify the pages and Using CSS

* Renew the query in single-movie and single-star

# Video URL:
https://youtu.be/gOCopc1z1uA

# CS 122B Project 1
Zhentao Yang:

* Finish createtable.sql

* Finish query.sql

* Record and Upload Youtube Video

* Set up AWS

David Liu:

* Code Movie List Page, Single Movie Page, Single Star Page

* Beautify the pages and Using CSS

* Debug code

# Video URL:
https://youtu.be/02SEspR2b5c

