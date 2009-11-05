// define Techbook bean mapping 

BeanDefinition = [
    // class of bean (used by hibernate for ORM)
  	"bean.class"     :      "org.mueller.booklibrary.Techbook",  
  	
  	// property list for field display order, and other necessary info
  	"prop1"          :  	"keypath",
      "prop1.id"     :        "userspecified",    // specify this is a key, how key works
      "prop1.label"  :        "Book Key",         // override default label of "keypath"
  	"prop2"			 :      "title",
  	  "prop2.label"  :        "Title",
  	"prop3"			 :		"link",
  	"prop4"			 :      "categories",
  	  "prop4.label"  :        "Categories",
  	
  	"propX"          :      "listproperty",
  	  "propX.widget" :        "radio",
  	  "propX.source" :        ["plugin":"org.webgen.dataprovider.Static",
  	                           "values":[["Yes","Y"],["No","N"]]],
  	"propY"          :      "dropdownprop",
  	  "propY.widget" :        "select",
  	  "propY.source" :        ["plugin":"org.webgen.dataprovider.Sql",
  	                           "query":"SELECT name,id FROM names WHERE zip = $zip"],
  	  
];
