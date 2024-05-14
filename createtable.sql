CREATE DATABASE if not exists moviedb;
USE moviedb;
create table if not exists movies(
                                     id varchar(10) primary key NOT NULL DEFAULT '',
                                     title varchar(100) NOT NULL DEFAULT '',
                                     year integer NOT NULL,
                                     director varchar(100) NOT NULL DEFAULT ''
                                     );

create table if not exists stars(
                                    id varchar(10) primary key NOT NULL DEFAULT '',
                                    name varchar(100) NOT NULL DEFAULT '',
                                    birthYear integer NULL
                                    );

create table if not exists stars_in_movies(
                                              starId varchar(10) NOT NULL DEFAULT '',
                                              movieId varchar(10) NOT NULL DEFAULT '',
                                              FOREIGN KEY (starId) REFERENCES stars(id),
                                              FOREIGN KEY (movieId) REFERENCES movies(id)
                                              );

create table if not exists genres(
                                     id integer primary key AUTO_INCREMENT,
                                     name varchar(32) NOT NULL DEFAULT ''
                                     );

create table if not exists genres_in_movies(
                                               genreId integer NOT NULL,
                                               movieId varchar(10) NOT NULL DEFAULT '',
                                               FOREIGN KEY (genreId) REFERENCES genres(id),
                                               FOREIGN KEY (movieId) REFERENCES movies(id)
                                               );

create table if not exists creditcards(
                                          id varchar(20) primary key NOT NULL DEFAULT '',
                                          firstName varchar(50) NOT NULL DEFAULT '',
                                          lastName varchar(50) NOT NULL DEFAULT '',
                                          expiration date NOT NULL);

create table if not exists customers(
                                        id integer primary key AUTO_INCREMENT,
                                        firstName varchar(50) NOT NULL DEFAULT '',
                                        lastName varchar(50) NOT NULL DEFAULT '',
                                        ccId varchar(20) NOT NULL DEFAULT '',
                                        address varchar(200) NOT NULL DEFAULT '',
                                        email varchar(50) NOT NULL DEFAULT '',
                                        password varchar(20) NOT NULL DEFAULT '',
                                        FOREIGN KEY (ccId) REFERENCES creditcards(id)
                                        );

create table if not exists sales(
                                    id integer primary key AUTO_INCREMENT,
                                    customerId integer NOT NULL,
                                    movieId varchar(10) NOT NULL DEFAULT '',
                                    saleDate date NOT NULL,
                                    FOREIGN KEY (customerId) REFERENCES customers(id),
                                    FOREIGN KEY (movieId) REFERENCES movies(id));

create table if not exists ratings(
                                      movieId varchar(10) primary key NOT NULL DEFAULT '',
                                      rating float NOT NULL,
                                      numVotes integer NOT NULL,
                                      FOREIGN KEY (movieId) REFERENCES movies(id)
                                      );