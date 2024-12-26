# Puppies API

Welcome to the Puppies API! This is a Spring Boot application that allows users to manage posts about their dogs. Users can create posts, like posts, and view posts created by themselves or others.

## Features

- User authentication
- Create and view posts
- Like posts
- Fetch user profiles
- Fetch liked posts and user-made posts
- Image upload as Base64

## Requirements

- Java Development Kit (JDK) 17 or higher
- Maven 3.6.0 or higher
- H2 Database (embedded, for development purposes)

## Installation

**Clone the Repository**
   ```bash
   git clone https://your-repository-url.git
   cd puppies-api
   ```

**Option 1 running with maven**
```
   mvn install
   mvn spring-boot:run
```

**Option 2 running the JAR**
```
    mvn clean package
    cd target
    java -jar puppies-api-0.0.1-SNAPSHOT.jar
```

**APIs**
## Accessing the Application

Once the application is running, you can access the API at: `http://localhost:8080`
### API Testing

To interact with the API, you can use tools like [Postman](https://www.postman.com/) or [Insomnia](https://insomnia.rest/), or make requests directly using `curl` in your command line.

### Example `curl` Requests

- **Fetch User Profile**
  ```bash
  curl --location --request GET 'http://localhost:8080/api/profile' \
  --header 'Authorization: Bearer <your_token>'
  ```
  ```bash
    curl --location --request GET 'http://localhost:8080/api/posts/liked' \
    --header 'Authorization: Bearer <your_token>'
  ```
  ```bash
    curl --location --request GET 'http://localhost:8080/api/posts/made' \
    --header 'Authorization: Bearer <your_token>'
  ```
  ```bash
    curl --location --request POST 'http://localhost:8080/api/posts' \
    --header 'Authorization: Bearer <your_token>' \
    --form 'image=@/path/to/your/image.jpg' \
    --form 'content=This is the content of the post' \
    --form 'date=2024-10-10T00:00:00'
  ```
  ```bash
    curl --location --request GET 'http://localhost:8080/api/posts/{postId}'
  ```

## setup localstack for S3 use
```bash
docker-compose up

awslocal s3api create-bucket --bucket sample-bucket

awslocal s3api list-buckets
```

### S3 use by cli
```bash
awslocal s3api put-object \
--bucket sample-bucket \
--key cat01.jpg \
--body cat01.jpg

awslocal s3api list-objects \
--bucket sample-bucket
awslocal s3 presign s3://sample-bucket/image.jpg
```
