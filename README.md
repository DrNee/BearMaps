# BearMaps
Berkeley Map Routing with Search, Directions, and Autocomplete

![Imgur](https://i.imgur.com/7A1ARoD.jpg)

This project was completed for CS61B and features search with autocomplete, turn-by-turn directions, and more for the Berkeley area!
In particular, I completed the backend work listed below and the rest was skeleton generously provided by course staff:

###
| Backend File | Purpose |
| --- | --- |
| [Rasterer](https://github.com/LanceSanity/Berkeley-CS61B-Audit/blob/master/proj3/src/main/java/Rasterer.java) | Renders map images given a user's requested area and level of zoom |
| [GraphDB](https://github.com/LanceSanity/Berkeley-CS61B-Audit/blob/master/proj3/src/main/java/GraphDB.java) | Graph representation of the contents of [Berkeley OSM](https://github.com/Berkeley-CS61B/library-sp18/tree/proj3/data). Implemented an Autocomplete system using a Trie data structure, which allows matching a prefix to valid location names in O(k) time, where k is the number of words sharing the prefix.|
| [GraphBuildingHandler](https://github.com/LanceSanity/Berkeley-CS61B-Audit/blob/master/proj3/src/main/java/GraphBuildingHandler.java) | Handler used by SAX parser to parse Nodes and Ways from Berkeley OSM file |
| [Router](https://github.com/LanceSanity/Berkeley-CS61B-Audit/blob/master/proj3/src/main/java/Router.java) | Uses A* search algorithm to find the shortest path between two points in Berkeley; uses shortest path to generate navigation directions. |
