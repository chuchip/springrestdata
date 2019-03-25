**Spring Boot** ofrece un fantástico soporte para acceder a los datos con JPA a través de sus interfaces, del tipo [Repository](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/Repository.html). Si a esto le añadimos la facilidad con que se crean servicios REST, como explicaba en la entrada [http://www.profesor-p.com/2018/10/11/aplicacion-crud-en-kotlin-con-springboot/]( http://www.profesor-p.com/2018/10/11/aplicacion-crud-en-kotlin-con-springboot/) podremos hacer una aplicación ofreciendo una API para acceder a nuestra base de datos preferida con muy poco código.

Pero si queremos implementar [HATEOAS](https://spring.io/projects/spring-hateoas)  en nuestro proyecto p si hay muchos criterios sobre los que debemos acceder a los datos, deberemos escribir bastante código. Para solucionar este problema **Spring Boot** provee el paquete [Spring Data Rest](https://docs.spring.io/spring-data/rest/docs/current/reference/html/) con el cual con apenas código podremos crear una API para acceder a una tabla de nuestra base de datos.

En el ejemplo que planteare en esta entrada veremos como poder realizar el mantenimiento de una tabla de clientes (customers)  sin escribir ni una sola línea para definir las diferentes APIS REST.

### Creando el proyecto

Como siempre, podremos ir a la pagina  https://start.spring.io para crear nuestro proyecto **maven**. Para el proyecto de ejemplo que vamos a crear, deberemos incluir las siguientes dependencias:

**Spring Data Rest**, **H2**, **JPA**, **Lombok** como se ve en la siguiente captura de pantalla



![Creando el proyecto](.\starters.png)



Una vez tengamos hayamos importado el proyecto en nuestro IDE preferido, deberemos modificar el fichero **application.properties** para definir ciertos parámetros:

```
#H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2
#JPA
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=create-drop
# Datasource
spring.datasource.url=jdbc:h2:file:~/test
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
# Data Rest
spring.data.rest.base-path: /api
```

De este fichero la única entrada relativa a la librería **Data Rest** es la última. En ella especificamos la dirección donde se deben implementar las llamadas **REST** para acceder a las diferentes tablas que implemente nuestra aplicación. Es decir, en nuestro caso, se accederá a través de la URL: http://localhost:8080/api

Las demás líneas configuran la base de datos H2 que usaremos, así como ciertas propiedades de JPA.



### Estructura del proyecto

Nuestro proyecto final tendrá la siguiente estructura:

![Estructura del proyecto](.\estructura.png)

Como se puede ver, definiremos dos tablas (entities) ,  que son: **City** y **Customer**. También definimos los correspondientes repositorios **CustomerRepository** y **CityRepository**

La clase **CityEntity** es la siguiente:

```java
@Entity
@Data
@RestResource(rel="customers", path="customer")
public class CustomerEntity {	
	@Id
	long id;
	
	@Column
	String name;
	
	@Column
	String address;
	
	@Column
	String telephone;
	
	@OneToOne
	CityEntity city;
}

```

La única particularidad es la línea **@RestResource** donde con el parámetro **rel** especificamos como debe llamarse el objeto en la salida JSON. Con el parámetro **path** se indica donde se debe realizar la petición.

Así, para acceder a este recurso deberemos acceder a: http://localhost:8080/api/customer . Si realizamos una petición **GET**  obtendríamos la siguiente salida:

```
> curl -s http://localhost:8080/api/customer
{
  "_embedded" : {
    "customers" : [ {
      "name" : "Name of Customer 1",
      "address" : "Address of Customer 1",
      "telephone" : "Telephone of Customer 1",
      "city" : null,
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/api/customer/1"
        },
        "customerEntity" : {
          "href" : "http://localhost:8080/api/customer/1"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/api/customer"
    },
    "profile" : {
      "href" : "http://localhost:8080/api/profile/customer"
    },
    "search" : {
      "href" : "http://localhost:8080/api/customer/search"
    }
  }
}
```

Si no pusiéramos la etiqueta **@RestResource** Spring presentaría la entidad en http://localhost:8080/api/customerEntities es decir usaría el nombre de la clase, poniéndolo en plural , por lo cual le añade '*es'*. 

Para saber los recursos que están publicados siempre podremos llamar a la URL: http://localhost:8080/api lo cual nos mostrara la siguiente salida:

```
> curl -s http://localhost:8080/api/
{
  "_links" : {
    "customers" : {
      "href" : "http://localhost:8080/api/customer"
    },
    "profile" : {
      "href" : "http://localhost:8080/api/profile"
    }
  }
}
```

El  *profile* se refiere a lo definido en el [RFC 6906](https://tools.ietf.org/html/rfc6906), donde se incluye detalles de la aplicación, pero no tratare de ello en esta entrada.

