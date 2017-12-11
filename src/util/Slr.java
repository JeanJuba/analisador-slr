package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Slr {

    private String inicial;
    private String inicialMod;
    private String[] productions;
    private Map<String, Map<Integer, List<String>>> mapaProducoes;
    private Map<String, List<String>> mapaCanonico;
    private List<String> entrada;

    List<Estado> canonicos;

    private Map<String, Estado> mapaGoTo;

    private int contadorEntrada = 0;
    private int indexCounterInterno = 0;
    private int indexCounterExterno = 0;

    private Estado estadoInicial;
    private Estado estadoFinal;
    private EstadoNodo nodo;

    private String comparator;

    public Slr(String[] productions, String[] mensagem) {

        this.productions = productions;
        this.entrada = new ArrayList<>(Arrays.asList(mensagem));
        createMap();
        goTo();
    }

    private void createMap() {
        mapaProducoes = new LinkedHashMap<>();
        inicial = productions[0].substring(0, productions[0].indexOf("->"));
        inicialMod = inicial + "'";
        for (String s : productions) {
            System.out.println(s);
            Map<Integer, List<String>> m = new HashMap<>();
            String[] split = s.substring(s.indexOf("->") + 2, s.length()).split("\\|");

            for (String piece : split) {
                String[] splitStr = piece.split("\\s+");
                m.put(contadorEntrada, new ArrayList<>(Arrays.asList(splitStr)));
                contadorEntrada++;
            }
            mapaProducoes.put(s.substring(0, s.indexOf("->")), m);
        }
    }

    private void goTo() {
        //Estado est = new Estado();
        initCanonicos();
        System.out.println("\nInicia Estados");
        Estado estado = estadoInicial;
        estadoFinal = estadoInicial;
        do {
            System.out.println("\nGoto: " + estado.getGoTo());
            System.out.println("Estado: " + estado.getStateName());
            if (estado.getValue() != null) {
                for (EstadoNodo n : estado.getValue()) {
                    List<EstadoNodo> nodeList = new ArrayList<>();
                    System.out.println("\n" + n.getNaoTerminalEsquerda() + " " + n.getProducaoDividida().toString());
                    Estado novoEstado = new Estado(); //Novo estado
                    int dotListIndex = findDotInList(n.getProducaoDividida());

                    if (!isDotEndOfString(n.getProducaoDividida(), dotListIndex)) { //Verifica se o ponto já percorreu a produção
                        List<String> temp = new ArrayList<>(n.getProducaoDividida());
                        System.out.println("Antes ponto movido: " + temp.toString());
                        temp.set(dotListIndex, temp.get(dotListIndex).replace(".", ""));

                        String symbolAfterDot = temp.get(dotListIndex);
                        System.out.println("Local ponto: " + temp.get(dotListIndex));

                        List<EstadoNodo> nodeListGerada = null;
                        if (temp.size() > 1 && dotListIndex + 1 < temp.size()) {
                            temp.set(dotListIndex + 1, "." + temp.get(dotListIndex + 1));
                            System.out.println("Moveu index ponto: " + temp.toString());
                            nodeListGerada = ntAfterDot(temp, dotListIndex + 1);
                        } else {
                            temp.set(dotListIndex, temp.get(dotListIndex) + ".");
                            System.out.println("Ponto no final: " + temp.toString());
                        }
                        System.out.println("Ponto movido: " + temp.toString());

                        EstadoNodo node = new EstadoNodo();
                        node.setNaoTerminalEsquerda(n.getNaoTerminalEsquerda());
                        node.setProducaoDividida(temp); //Produção dividida já com o ponto movido
                        nodeList.add(node);

                        if (nodeListGerada != null) {
                            System.out.println("NodeList Gerada.");
                            for (EstadoNodo estNode : nodeListGerada) {
                                nodeList.add(estNode);
                            }
                        }

                        createStringRepresentation(nodeList);
                        Estado e = findExistingState(createStringRepresentation(nodeList));
                        if (e == null) {
                            novoEstado.setStateName("I" + indexCounterExterno);
                            System.out.println("Novo Estado: " + novoEstado.getStateName());
                            for (EstadoNodo no : nodeList) {
                                System.out.println(no.getProducaoDividida().toString());
                            }
                            novoEstado.setGoTo(estado.getStateName() + ", " + symbolAfterDot);
                            indexCounterExterno++;
                            novoEstado.setValue(nodeList);
                            novoEstado.setNextState(null);
                        } else {
                            novoEstado.setGoTo(estado.getStateName() + ", " + symbolAfterDot);
                            novoEstado.setStateName(e.getStateName());
                            novoEstado.setValue(null);
                        }

                        estadoFinal.setNextState(novoEstado);
                        estadoFinal = novoEstado;
                    }
                }
            }
            estado = estado.getNextState();
            System.out.println("===\n");
        } while (estado != null);
    }

    private boolean isDotEndOfString(List<String> lista, int dotListIndex) {
        String str = lista.get(dotListIndex);
        if (str.indexOf(".") == str.length() - 1) {
            return true;
        } else {
            return false;
        }
    }

    private int findDotInList(List<String> lista) {
        for (int i = 0; i < lista.size(); i++) {
            //System.out.println("Lista(i): " + lista.get(i));
            if (lista.get(i).contains(".")) {
                return i;
            }
        }
        return -1;
    }

    private List<EstadoNodo> ntAfterDot(List<String> temp, int dotListIndex) {
        List<EstadoNodo> lista = new ArrayList<>();
        String str = temp.get(dotListIndex);

        if (str.contains(".") && str.indexOf(".") + 1 < str.length()) {
            char symbolAfterDot = str.charAt(str.indexOf(".") + 1);
            System.out.println("Nao terminal procurado: " + symbolAfterDot);
            Map<Integer, List<String>> m = mapaProducoes.get(String.valueOf(symbolAfterDot));
            if (m != null) {
                for (Map.Entry<Integer, List<String>> e : m.entrySet()) {
                    EstadoNodo novoNodo = new EstadoNodo();
                    novoNodo.setNaoTerminalEsquerda(String.valueOf(symbolAfterDot));
                    List<String> prodSplited = new ArrayList<>(e.getValue());
                    prodSplited.set(0, "." + prodSplited.get(0));
                    novoNodo.setProducaoDividida(prodSplited);
                    lista.add(novoNodo);

                    char charAfterDot = prodSplited.get(0).charAt(1);
                    System.out.println("to be found: " + charAfterDot);
                    if (mapaProducoes.get(String.valueOf(charAfterDot)) != null) {
                        System.out.println("found: " + charAfterDot);
                        for (Map.Entry<Integer, List<String>> map : mapaProducoes.get(String.valueOf(charAfterDot)).entrySet()) {
                            novoNodo = new EstadoNodo();
                            novoNodo.setNaoTerminalEsquerda(String.valueOf(charAfterDot));
                            prodSplited = new ArrayList<>(map.getValue());
                            prodSplited.set(0, "." + prodSplited.get(0));
                            novoNodo.setProducaoDividida(prodSplited);
                            lista.add(novoNodo);
                        }
                    }

                }
            }
        }
        return lista;
    }

    private String createStringRepresentation(List<EstadoNodo> nodos) {
        String represetation = "";
        for (EstadoNodo n : nodos) {

            represetation = represetation + n.getNaoTerminalEsquerda() + n.getProducaoDividida().toString();
        }

        System.out.println("Representação estado em string: " + represetation);
        return represetation;
    }

    private Estado findExistingState(String stringRepresentation) {
        System.out.println("Recebido: " + stringRepresentation);
        Estado estado = estadoInicial;

        while (estado != null) {
            List<EstadoNodo> nodos = estado.getValue();

            String estadoString = "";
            if (nodos != null) {
                for (EstadoNodo node : nodos) {
                    estadoString = estadoString + node.getNaoTerminalEsquerda() + node.getProducaoDividida().toString();
                }

                System.out.println("Encontrado: " + estadoString);
                if (estadoString.equals(stringRepresentation)) {
                    System.out.println("já existe");
                    return estado;
                }
            }
            estado = estado.getNextState();
        }

        return null;
    }

    private void initCanonicos() {
        //Estado estado = new Estado();
        List<EstadoNodo> line = new ArrayList<>();
        estadoInicial = new Estado();
        nodo = new EstadoNodo();
        nodo.setNaoTerminalEsquerda(inicialMod); //S'
        List<String> l = new ArrayList<>();
        l.add(".S");
        nodo.setProducaoDividida(l);

        line.add(nodo);

        mapaProducoes.get(inicial).entrySet().forEach((e) -> {
            nodo = new EstadoNodo();
            nodo.setNaoTerminalEsquerda(inicial);
            List<String> temp = new ArrayList<>(e.getValue());
            temp.set(0, "." + temp.get(0));
            nodo.setProducaoDividida(temp);
            line.add(nodo);
        });
        estadoInicial.setNextState(null);
        estadoInicial.setStateName("I" + indexCounterExterno);
        estadoInicial.setGoTo("I" + indexCounterExterno);
        estadoInicial.setValue(line);
        indexCounterExterno++;
    }

    //<editor-fold defaultstate="collapsed" desc="PRINT">
    public void printMapa() {
        System.out.println("\n=== MAPA ===");
        for (Map.Entry<String, Map<Integer, List<String>>> e : mapaProducoes.entrySet()) {
            e.getValue().entrySet().forEach((f) -> {
                System.out.println(f.getKey() + e.getKey() + " : " + f.getValue().toString());
            });
        }
    }

    public void printCanonicos() {
        System.out.println("\n=== Caninicos ===");
        Estado estado = estadoInicial;
        System.out.println("\nNome: " + estado.getStateName());
        if (estado.getValue() != null) {
            for (EstadoNodo n : estado.getValue()) {
                System.out.println(n.getNaoTerminalEsquerda() + " " + n.getProducaoDividida().toString());
            }
            System.out.println("\n");
        }
    }

    public void printEstados() {
        System.out.println("\n=== ESTADOS ===");
        Estado est = estadoInicial;
        while (est != null) {
            System.out.println("Goto: " + est.getGoTo());
            if (est.getValue() != null) {
                est.getValue().forEach((e) -> {
                    System.out.print(e.getNaoTerminalEsquerda() + e.getProducaoDividida().toString() + "\n");

                });
            }
            System.out.println("Estado: " + est.getStateName() + "\n");
            est = est.getNextState();
        }

    }

    public void printMensagem() {
        System.out.println("\n=== MENSAGEM ===");
        System.out.print(entrada.toString());
    }
    //</editor-fold>
}
