# shibboleth-idp-mailman-connector
Provide the following parameters from Shibboleth attribute-resolver.xml file : hostname, username,password and port number.
All this parameters will be provided as input attributes in <resolver:DataConnector>element in attribute-resolver.xml file

The API checks only public(advertised) maliman groups. Member's username is passed as a parameter in attribute-resolver.xml usign variables

The final output is a list of Groups the user belongs to. Beside each group additional info is provided if the user is a group admin.
