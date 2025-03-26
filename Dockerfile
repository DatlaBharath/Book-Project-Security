FROM openjdk:17-oracle
COPY ./target/booksecurity-0.0.1-SNAPSHOT.jar booksecurity.jar
CMD ["java","-jar","booksecurity.jar"]
