**Spring Boot** ofrece un fantástico soporte para acceder a los datos con JPA a través de sus interfaces, del tipo [Repository](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/Repository.html). Si a esto le añadimos la facilidad con que se crean servicios REST, como explicaba en la entrada [http://www.profesor-p.com/2018/10/11/aplicacion-crud-en-kotlin-con-springboot/]( http://www.profesor-p.com/2018/10/11/aplicacion-crud-en-kotlin-con-springboot/) podremos hacer una aplicación ofreciendo una API para acceder a nuestra base de datos preferida con muy poco código.

Pero si queremos implementar [HATEOAS](https://spring.io/projects/spring-hateoas)  en nuestro proyecto p si hay muchos criterios sobre los que debemos acceder a los datos, deberemos escribir bastante código. Para solucionar este problema **Spring Boot** provee el paquete [Spring Data Rest](https://docs.spring.io/spring-data/rest/docs/current/reference/html/) con el cual con apenas código podremos crear una API para acceder a una tabla de nuestra base de datos.

¡¡Vamos a ver como!!

### Creando el proyecto

Como siempre, podremos ir a la pagina  https://start.spring.io para crear nuestro proyecto **maven**. Para el proyecto de ejemplo que vamos a crear, deberemos incluir las siguientes dependencias:

**Spring Data Rest**, **H2**, **JPA**, **Lombok** como se ve en la siguiente captura de pantalla



![](.\starters.png)



Una vez tengamos hayamos importado el proyecto en nuestro IDE preferido, deberemos modificar el fichero **application.properties** para definir la conexion 

