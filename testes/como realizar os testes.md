#### Requisições



ara reproduzir os resultados, exporte o arquivo de coleção do Postman em anexo e dentro do ambiente verifique se as seguintes variaveis estão configuradas:



baseUrl: http://localhost:8080

token: (deixe vazio, mas caso seja solicitado coloque qualquer valor)



Na requisição de login cole o seguinte script em "After response" caso esteja vazio:

const res = pm.response.json();

pm.collectionVariables.set("token", res.accessToken);



#### Configuração de proxy



No postman habilite a opção "Use custom proxy configuration"  e marque apenas HTTP, e configure do  seguinte modo:

Proxy server http://localhost:8090

Proxy auth: desligado



Certifique-se que Proxy bypass esteja vazio.

No Owasp zap va em tools > network > Local Servers/Proxies e configure:

Address: localhost

Port: 8090


Ainda nas opções, selecione a aba "HUD" e desmarque a caixa "Enable when using ZAP Desktop", isso pode fazer com que requisições HTTP se tornem HTTPS e falhem antes mesmo de chegar a API. Após isso, ir até tools > Replacer option e adicionar uma regra para o token JWT, da seguinte forma:
Description: auth

Match type: Request Header (will add if not present)

Match string: Authorization

Replacement String: Bearer <colar accessToken gerado pela requisição de login do postman>





Após isso, basta iniciar a API (recomendo usar perfil de testes) e rodar a coleção completa no postman; depois, dentro do zap, clicar com o botão direito em http://localhost:8080 e selecionar active scan para iniciar a varredura

