Verimi Integration für Liferay
==============================

Diese Module repräsentieren eine mögliche Integration von [Verimi](https://verimi.de/) in Liferay. 

Verimi scheint den Namen einer Person als vollständigen Namen zu übertragen, während die OpenID Connect Implementierung von Liferay den Namen in
zwei getrennten Feldern erwartet. Das vorliegende Modul trennt daher den Namen in Vor- und Nachnamen auf. Außerdem wurde eine Unterstützung für die
Übernahme des Geburtsdatums ergänzt.

Installation
------------

Nach dem Deployment der Module müssen die im Modul `verimi-openid-connect-service-handler` abgelegten Dateien nach `liferay.home/osgi/configs` kopiert werden:

* `com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectServiceHandlerImpl.config`

Anwendung
---------

Verimi ist ein OpenID Connect Provider. Für die Einrichtung muss daher ein neuer OpenID Connect Provider in Liferay konfiguriert werden. Die dazu notwendigen
Daten müssen von Verimi bereitgestellt werden.

Neue OpenID Connect Provider werden unter Kontrollbereich > Konfiguration > Systemeinstellungen > Sicherheit > SSO hinterlegt. Generell wird OpenID Connect hier
unter dem Punkt *OpenID Connect* aktiviert. Unter *Provider OpenID Connect* kann ein neuer Eintrag für Verimi hinterlegt werden.

Als Scopes (Geltungsbereiche) unterstützt das vorliegende Modul die folgenden Einträge:
    
    openid name email birthdate

Liferay-Versionen
-----------------

Dieses Plugin wurde erfolgreich in der folgenden Liferay-Version getestet:

* Liferay CE 7.1.3 GA4

Hinweise für Entwickler
-----------------------

Hintergrundinformationen zur Entwicklung können diesem Blog-Post entnommen werden: https://liferay.dev/blogs/-/blogs/integrating-verimi-with-liferay