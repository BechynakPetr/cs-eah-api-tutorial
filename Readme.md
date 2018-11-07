Tutorial - how to connect to Csas API
=====================================
Java skeleton web-based application for connection to Csas.
For more information visit: https://developers.erstegroup.com/docs/guides/csas-getting-started

This is a Java 8, spring-boot project. To run the app execute:

`mvn spring-boot:run`

## Testing the skeleton app
Open your browser and type `http://localhost:8080/corpaccounts`, 
`http://localhost:8080/persaccounts`, `http://localhost:8080/corptranshist`
The controller will serve your request and call bank sandbox API and return it to browser.

## Settings 
Basic settings is in application.properties file. By default, csas sandbox is preset. 
- To change it to your app, 
    - change "your app" section
    - change urls in "endpoints" section
- To set proxy, change "proxy" section (leave empty for no proxy)

## Other
- The paging and sorting in responses does't work in sandbox environment.
- For simplicity the /auth call is set not to follow redirection, but accepts 
whole 302 response instead and uses the code value