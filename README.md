# Проект PrintMe
<p align="center">
<img width="150" height="150" alt="Printer logo" src="https://github.com/RomchiLolchi/PrintMeServer/blob/master/composeApp/src/wasmJsMain/resources/printer.png">
</p>

**Цель проекта** - добавление поддержки облачной печати принтерам, которые изначально не поддерживали эту технологию.
## О репозитории
Здесь хранится полный код сервлета, который загружается в Apache Tomcat на необходимое устройство (в нашем случае на Raspberry Pi 4) и обрабатывает запросы на печать в пределах локальной сети.\
\
Проект почти полностью написан на языке Kotlin с использованием технологии Kotlin Multiplatform. Присутствует разделение на 3 модуля: ```shared``` - модуль общего кода, ```composeApp``` - модуль с кодом для отрисовки интерфейса с помощью фреймворка Compose Multiplatform и ```server``` - модуль с кодом сервера.
### ```shared```
В модуле находится лишь один класс [```ServiceAttributeSet.kt```](https://github.com/RomchiLolchi/PrintMeServer/blob/master/shared/src/commonMain/kotlin/ServiceAttributeSet.kt), который используется для клиент-серверного взаимодействия.
### ```composeApp```
Модуль разбит на подмодули ```commonMain``` - модуль с кодом, используемым при отрисовке всех интерфесов проекта и ```wasmJsMain``` - модуль кода для отрисовки интерфейса веб-страницы.
### ```server```
Модуль с кодом сервера. Использует библиотеку Ktor с модулями CORS, Routing и ContentNegotiation. Также возвращает веб-страницу, используя скомпилированный (транслированный) код модуля ```wasmJsMain```, который находится в папке [```libs/wasm```](https://github.com/RomchiLolchi/PrintMeServer/tree/master/server/src/main/resources/libs/wasm). Одноимённый объект в файле [```PrintingTools.kt```](https://github.com/RomchiLolchi/PrintMeServer/blob/master/server/src/main/kotlin/site/romchilolchi/PrintingTools.kt) ответственнен за печать (использует содержимое пакета ```javax.print```)

 ## Путь пользователя
 1. Пользователь заходит на веб-страницу
 2. Пользователь выбирает принтер
 3. Пользователь выбирает атрибуты печати
 4. Пользователь выбирает и загружает файл для печати
 5. Пользователь начинает печать
