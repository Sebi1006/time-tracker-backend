# Time Tracker Backend

This is the backend project of a cloud-native time tracking tool developed in the class Cloud Application Development at the University of Applied Sciences HTWG Konstanz. The time tracker app is able to track the working hours of each employee in an organization, is highly scalable and can easily be run in the cloud using providers such as AWS.

## Technical details

The backend is a Spring Boot application (Java project) which uses the build automation tool Maven for explicit declaration and isolation of dependencies in *pom.xml*. It mainly functions as an API for requests sent from the frontend. Most of the configuration is stored in the Spring Boot properties file *application.yml*. The application can be run on a standard cloud platform, in this case in an EKS Kubernetes cluster. With Kubernetes it is possible to provide, scale and manage Docker containers, whereas one container represents the backend API of the main app. It implements a multi-user, multi-tenant Software-as-a-Service (SaaS) as well as the five essential characteristics of a cloud service.
