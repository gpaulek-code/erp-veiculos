package com.revendaauto.erp.controller;

import com.revendaauto.erp.dto.VeiculoFormDTO;
import com.revendaauto.erp.enums.StatusVeiculo;
import com.revendaauto.erp.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;

@Controller
@RequestMapping("/importacao")
@RequiredArgsConstructor
public class ImportacaoController {

    private final VeiculoService veiculoService;

    // Padrão de placa brasileira (antiga AAA9999 e Mercosul AAA9A99)
    private static final Pattern PLACA_PATTERN = Pattern.compile(
        "\\b([A-Z]{3}[-\\s]?\\d[A-Z0-9]\\d{2})\\b", Pattern.CASE_INSENSITIVE
    );
    // Padrão de ano (1950-2099)
    private static final Pattern ANO_PATTERN = Pattern.compile("\\b(19[5-9]\\d|20[0-9]\\d)\\b");
    // Padrão de valor monetário
    private static final Pattern VALOR_PATTERN = Pattern.compile(
        "R?\\$?\\s?(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2}))"
    );
    // Padrão de quilometragem (ex: 45000, 45.000, 45000km)
    private static final Pattern KM_PATTERN = Pattern.compile(
        "\\b(\\d{1,3}(?:[.,]\\d{3})*)\\s*(?:km|kms|quilômetros)?\\b", Pattern.CASE_INSENSITIVE
    );

    // Palavras-chave para identificar colunas (aliases inteligentes)
    private static final Map<String, List<String>> ALIASES = new LinkedHashMap<>();
    static {
        ALIASES.put("placa",         List.of("placa","plate","matricula","licença","licence","license"));
        ALIASES.put("marca",         List.of("marca","make","fabricante","montadora","brand","fabricação"));
        ALIASES.put("modelo",        List.of("modelo","model","veiculo","veículo","carro","vehicle","descricao","descrição","nome"));
        ALIASES.put("ano",           List.of("ano","year","ano fab","anofab","fabricação","ano/mod","ano mod","ano modelo","ano fabricação","ano fabricação/modelo"));
        ALIASES.put("cor",           List.of("cor","color","colour","pintura"));
        ALIASES.put("quilometragem", List.of("km","kms","quilometragem","quilômetros","kilometragem","kilometros","odometro","odômetro","hodometro","hodômetro","milhagem"));
        ALIASES.put("valor",         List.of("valor","preço","preco","price","venda","vlr","val","custo","r$","valor venda","preço venda","preco venda","valor de venda"));
        ALIASES.put("situacao",      List.of("situação","situacao","status","estado","posição","posicao","sit"));
    }

    @GetMapping
    public String form() {
        return "importacao/form";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("arquivo") MultipartFile arquivo,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            String nome = arquivo.getOriginalFilename() != null
                ? arquivo.getOriginalFilename().toLowerCase() : "";

            List<VeiculoImportado> veiculos;

            if (nome.endsWith(".xlsx")) {
                veiculos = lerExcel(arquivo.getInputStream(), false);
            } else if (nome.endsWith(".xls")) {
                // Tenta primeiro como XML Spreadsheet, depois como HSSF binário
                byte[] bytes = arquivo.getInputStream().readAllBytes();
                veiculos = lerXls(bytes);
            } else if (nome.endsWith(".pdf")) {
                veiculos = lerPdf(arquivo.getInputStream());
            } else {
                redirectAttributes.addFlashAttribute("erro",
                    "Formato inválido. Envie .xlsx, .xls ou .pdf");
                return "redirect:/importacao";
            }

            if (veiculos.isEmpty()) {
                redirectAttributes.addFlashAttribute("erro",
                    "Nenhum veículo encontrado no arquivo. Verifique o formato.");
                return "redirect:/importacao";
            }

            model.addAttribute("veiculos", veiculos);
            model.addAttribute("statusList", StatusVeiculo.values());
            return "importacao/revisar";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro",
                "Erro ao processar arquivo: " + e.getMessage());
            return "redirect:/importacao";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam Map<String, String> params,
                         RedirectAttributes redirectAttributes) {
        int salvos = 0, erros = 0;
        int total = Integer.parseInt(params.getOrDefault("total", "0"));

        for (int i = 0; i < total; i++) {
            try {
                String placa    = limpar(params.get("placa_" + i));
                String marca    = limpar(params.get("marca_" + i));
                String modelo   = limpar(params.get("modelo_" + i));
                String anoStr   = limpar(params.get("ano_" + i));
                String cor      = limpar(params.get("cor_" + i));
                String valorStr = limpar(params.get("valor_" + i));
                String statusStr= limpar(params.get("status_" + i));

                // Placa: gera placeholder se vazia para não bloquear importação
                if (placa.isBlank()) placa = "SEMPL" + String.format("%04d", i + 1);
                String placaFinal = placa.toUpperCase().replaceAll("[^A-Z0-9]", "");
                if (placaFinal.isBlank()) placaFinal = "SEMPL" + String.format("%04d", i + 1);

                VeiculoFormDTO dto = new VeiculoFormDTO();
                dto.setPlaca(placaFinal);
                dto.setMarca(marca.isBlank()   ? "Não informado" : marca);
                dto.setModelo(modelo.isBlank()  ? "Não informado" : modelo);
                dto.setAno(parseAno(anoStr));
                dto.setCor(cor.isBlank()       ? "Não informado" : cor);
                dto.setValor(parseValor(valorStr));

                // Status: prioriza o selecionado na tela, mas usa o detectado automaticamente
                StatusVeiculo status;
                try {
                    status = StatusVeiculo.valueOf(statusStr);
                } catch (Exception ex) {
                    status = StatusVeiculo.LOJA_1;
                }
                dto.setStatus(status);

                veiculoService.salvar(dto);
                salvos++;
            } catch (Exception e) {
                erros++;
            }
        }

        redirectAttributes.addFlashAttribute("sucesso",
            salvos + " veículo(s) importado(s) com sucesso!"
            + (erros > 0 ? " (" + erros + " com erro)" : ""));
        return "redirect:/veiculos";
    }

    // ─── Excel XLSX ───────────────────────────────────────────────────────────

    private List<VeiculoImportado> lerExcel(InputStream is, boolean hssf) throws Exception {
        List<VeiculoImportado> lista = new ArrayList<>();
        try (Workbook wb = hssf ? new HSSFWorkbook(is) : new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int headerRow = detectarLinhaHeader(sheet);
            Map<String, Integer> cols = mapearColunas(sheet.getRow(headerRow));
            for (int r = headerRow + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;
                VeiculoImportado v = extrairLinha(row, cols);
                lista.add(v);
            }
        }
        return lista;
    }

    private List<VeiculoImportado> lerXls(byte[] bytes) {
        // Tenta HSSF (binário)
        try {
            return lerExcel(new java.io.ByteArrayInputStream(bytes), true);
        } catch (Exception ignored) {}
        // Tenta XML Spreadsheet
        try {
            return lerXmlSpreadsheet(new java.io.ByteArrayInputStream(bytes));
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    // ─── XML Spreadsheet (formato antigo .xls salvo como XML) ─────────────────

    private List<VeiculoImportado> lerXmlSpreadsheet(InputStream is) throws Exception {
        List<VeiculoImportado> lista = new ArrayList<>();
        javax.xml.parsers.DocumentBuilderFactory dbf =
            javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(is);

        String NS = "urn:schemas-microsoft-com:office:spreadsheet";
        org.w3c.dom.NodeList rows = doc.getElementsByTagNameNS(NS, "Row");

        // Detectar linha de cabeçalho
        int headerIdx = -1;
        List<List<String>> allRows = new ArrayList<>();
        for (int i = 0; i < rows.getLength(); i++) {
            org.w3c.dom.Element row = (org.w3c.dom.Element) rows.item(i);
            org.w3c.dom.NodeList cells = row.getElementsByTagNameNS(NS, "Cell");
            List<String> vals = new ArrayList<>();
            for (int j = 0; j < cells.getLength(); j++) {
                org.w3c.dom.Element cell = (org.w3c.dom.Element) cells.item(j);
                org.w3c.dom.NodeList data = cell.getElementsByTagNameNS(NS, "Data");
                vals.add(data.getLength() > 0 && data.item(0).getTextContent() != null
                    ? data.item(0).getTextContent().trim() : "");
            }
            if (headerIdx < 0 && pareceHeader(vals)) {
                headerIdx = i;
            }
            allRows.add(vals);
        }

        if (headerIdx < 0 || headerIdx >= allRows.size()) headerIdx = 0;

        Map<String, Integer> cols = mapearColunasLista(allRows.get(headerIdx));
        for (int i = headerIdx + 1; i < allRows.size(); i++) {
            List<String> vals = allRows.get(i);
            if (vals.stream().allMatch(String::isBlank)) continue;
            lista.add(extrairDeLista(vals, cols));
        }
        return lista;
    }

    // ─── PDF ──────────────────────────────────────────────────────────────────

    private List<VeiculoImportado> lerPdf(InputStream is) throws Exception {
        List<VeiculoImportado> lista = new ArrayList<>();
        byte[] bytes = is.readAllBytes();
        PdfReader reader = new PdfReader(bytes);
        StringBuilder sb = new StringBuilder();
        for (int p = 1; p <= reader.getNumberOfPages(); p++) {
            sb.append(PdfTextExtractor.getTextFromPage(reader, p)).append("\n");
        }
        reader.close();

        for (String linha : sb.toString().split("\n")) {
            Matcher m = PLACA_PATTERN.matcher(linha);
            if (!m.find()) continue;

            VeiculoImportado v = new VeiculoImportado();
            v.placa = m.group().replaceAll("[\\s-]", "").toUpperCase();

            Matcher anoM = ANO_PATTERN.matcher(linha);
            if (anoM.find()) v.ano = anoM.group();

            Matcher valM = VALOR_PATTERN.matcher(linha);
            if (valM.find()) {
                String rawValor = valM.group(1);
                if (rawValor.contains(",")) {
                    v.valor = rawValor.replaceAll("[.]", "").replace(",", ".");
                } else {
                    v.valor = "00,00";
                }
            } else {
                v.valor = "00,00";
            }

            Matcher kmM = KM_PATTERN.matcher(linha.replaceAll(v.placa, ""));
            if (kmM.find()) v.quilometragem = kmM.group(1).replaceAll("[.,]", "");

            // Resto da linha sem placa/ano/valor = modelo
            String resto = linha.replaceAll(PLACA_PATTERN.pattern(), "")
                                .replaceAll(ANO_PATTERN.pattern(), "")
                                .replaceAll(VALOR_PATTERN.pattern(), "")
                                .replaceAll("[\\s]{2,}", " ").trim();
            if (!resto.isBlank()) {
                v.modelo = resto.trim();
                v.marca  = detectarMarca(v.modelo);
            } else {
                v.marca = "Não informado";
                v.modelo = "";
            }

            if (linha.toLowerCase().contains("vendido")) {
                v.temVendido = true;
            }

            v.situacao       = "";
            v.faltaPlaca         = v.placa.isBlank();
            v.faltaMarca         = v.marca.equals("Não informado");
            v.faltaModelo        = v.modelo.isBlank();
            v.faltaAno           = v.ano.isBlank();
            v.faltaCor           = v.cor.isBlank();
            v.faltaQuilometragem = v.quilometragem.isBlank();
            v.faltaValor         = v.valor.equals("00,00");

            lista.add(v);
        }
        return lista;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private int detectarLinhaHeader(Sheet sheet) {
        for (int r = 0; r <= Math.min(5, sheet.getLastRowNum()); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            List<String> vals = new ArrayList<>();
            for (int c = 0; c < row.getLastCellNum(); c++) {
                vals.add(cellStr(row, c).toLowerCase());
            }
            if (pareceHeader(vals)) return r;
        }
        return 0;
    }

    private boolean pareceHeader(List<String> vals) {
        long matches = vals.stream().filter(v -> {
            for (List<String> aliases : ALIASES.values()) {
                for (String a : aliases) if (v.contains(a)) return true;
            }
            return false;
        }).count();
        return matches >= 2;
    }

    private Map<String, Integer> mapearColunas(Row header) {
        Map<String, Integer> map = new HashMap<>();
        if (header == null) return map;
        for (int i = 0; i < header.getLastCellNum(); i++) {
            String nome = cellStr(header, i).toLowerCase().trim();
            for (Map.Entry<String, List<String>> e : ALIASES.entrySet()) {
                if (map.containsKey(e.getKey())) continue;
                for (String alias : e.getValue()) {
                    if (nome.contains(alias)) { map.put(e.getKey(), i); break; }
                }
            }
        }
        return map;
    }

    private Map<String, Integer> mapearColunasLista(List<String> header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            String nome = header.get(i).toLowerCase().trim();
            for (Map.Entry<String, List<String>> e : ALIASES.entrySet()) {
                if (map.containsKey(e.getKey())) continue;
                for (String alias : e.getValue()) {
                    if (nome.contains(alias)) { map.put(e.getKey(), i); break; }
                }
            }
        }
        // Se não achou placa, tenta detectar por conteúdo
        return map;
    }

    private VeiculoImportado extrairLinha(Row row, Map<String, Integer> cols) {
        VeiculoImportado v = new VeiculoImportado();
        v.placa         = get(row, cols, "placa");
        v.modelo        = get(row, cols, "modelo");
        v.marca         = detectarMarca(v.modelo);
        v.ano           = get(row, cols, "ano");
        v.cor           = get(row, cols, "cor");
        v.quilometragem = get(row, cols, "quilometragem");
        v.valor         = processarValorLogica(get(row, cols, "valor"));

        // Se não achou placa pela coluna, tenta detectar em todas as células
        if (v.placa.isBlank()) {
            for (int c = 0; c < row.getLastCellNum(); c++) {
                String val = cellStr(row, c);
                if (PLACA_PATTERN.matcher(val).matches()) { v.placa = val.toUpperCase(); break; }
            }
        }
        // Se não achou ano, tenta detectar
        if (v.ano.isBlank()) {
            for (int c = 0; c < row.getLastCellNum(); c++) {
                String val = cellStr(row, c);
                if (val.matches("(19[5-9]\\d|20[0-9]\\d)(/\\d{4})?")) {
                    v.ano = val.split("/")[0]; break;
                }
            }
        }
        // Se não achou valor, tenta detectar padrão monetário
        if (v.valor.isBlank() || v.valor.equals("00,00")) {
            String valorDetectado = "";
            for (int c = 0; c < row.getLastCellNum(); c++) {
                String val = cellStr(row, c);
                if (val.matches("\\d{4,}\\.?\\d*") && Double.parseDouble(val.replace(",",".")) > 1000) {
                    valorDetectado = val; break;
                }
            }
            v.valor = processarValorLogica(valorDetectado);
        }

        // Verificar se "Vendido" está escrito em qualquer lugar da linha
        for (int c = 0; c < row.getLastCellNum(); c++) {
            if (cellStr(row, c).toLowerCase().contains("vendido")) {
                v.temVendido = true;
                break;
            }
        }

        // Calcular campos faltantes
        v.situacao       = get(row, cols, "situacao");
        v.faltaPlaca         = v.placa.isBlank();
        v.faltaMarca         = v.marca.equals("Não informado");
        v.faltaModelo        = v.modelo.isBlank();
        v.faltaAno           = v.ano.isBlank();
        v.faltaCor           = v.cor.isBlank();
        v.faltaQuilometragem = v.quilometragem.isBlank();
        v.faltaValor         = v.valor.equals("00,00");

        return v;
    }

    private VeiculoImportado extrairDeLista(List<String> vals, Map<String, Integer> cols) {
        VeiculoImportado v = new VeiculoImportado();
        v.placa         = getL(vals, cols, "placa");
        v.modelo        = getL(vals, cols, "modelo");
        v.marca         = detectarMarca(v.modelo);
        v.ano           = getL(vals, cols, "ano");
        v.cor           = getL(vals, cols, "cor");
        v.quilometragem = getL(vals, cols, "quilometragem");
        v.valor         = processarValorLogica(getL(vals, cols, "valor"));

        // Detecção por conteúdo se coluna não mapeada
        if (v.placa.isBlank()) {
            for (String val : vals) {
                if (PLACA_PATTERN.matcher(val.trim()).matches()) { v.placa = val.trim().toUpperCase(); break; }
            }
        }
        if (v.ano.isBlank()) {
            for (String val : vals) {
                if (val.trim().matches("(19[5-9]\\d|20[0-9]\\d)(/\\d{4})?")) {
                    v.ano = val.trim().split("/")[0]; break;
                }
            }
        }

        // Verificar se "Vendido" está escrito em qualquer lugar da lista
        for (String val : vals) {
            if (val.toLowerCase().contains("vendido")) {
                v.temVendido = true;
                break;
            }
        }

        v.situacao       = getL(vals, cols, "situacao");
        v.faltaPlaca         = v.placa.isBlank();
        v.faltaMarca         = v.marca.equals("Não informado");
        v.faltaModelo        = v.modelo.isBlank();
        v.faltaAno           = v.ano.isBlank();
        v.faltaCor           = v.cor.isBlank();
        v.faltaQuilometragem = v.quilometragem.isBlank();
        v.faltaValor         = v.valor.equals("00,00");

        return v;
    }

    private String get(Row row, Map<String, Integer> cols, String campo) {
        Integer idx = cols.get(campo);
        return idx != null ? cellStr(row, idx) : "";
    }

    private String getL(List<String> vals, Map<String, Integer> cols, String campo) {
        Integer idx = cols.get(campo);
        return (idx != null && idx < vals.size()) ? vals.get(idx) : "";
    }

    private String cellStr(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return "";
        try {
            org.apache.poi.ss.usermodel.DataFormatter formatter = 
                new org.apache.poi.ss.usermodel.DataFormatter(new java.util.Locale("pt", "BR"));
            return formatter.formatCellValue(c).trim();
        } catch (Exception e) {
            return switch (c.getCellType()) {
                case NUMERIC -> {
                    double d = c.getNumericCellValue();
                    yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
                }
                case STRING  -> c.getStringCellValue().trim();
                case FORMULA -> {
                    try { yield String.valueOf((long) c.getNumericCellValue()); }
                    catch (Exception e2) { yield c.getStringCellValue().trim(); }
                }
                default -> "";
            };
        }
    }

    private String processarValorLogica(String valor) {
        if (valor == null || valor.isBlank()) {
            return "00,00";
        }
        if (valor.contains(",")) {
            return valor;
        }
        return "00,00";
    }

    public static String detectarMarca(String veiculo) {
        if (veiculo == null || veiculo.isBlank()) return "Não informado";
        String v = veiculo.toLowerCase();
        String[] words = v.split("[\\s_.,/\\(\\)-]+");
        Set<String> wordSet = new HashSet<>(Arrays.asList(words));

        // Chevrolet / GM
        if (wordSet.contains("astra") || wordSet.contains("onix") || wordSet.contains("tracker") || 
            wordSet.contains("corsa") || wordSet.contains("celta") || wordSet.contains("prisma") || 
            wordSet.contains("cruze") || wordSet.contains("cobalt") || wordSet.contains("spin") || 
            wordSet.contains("montana") || wordSet.contains("s10") || v.contains("s-10") || 
            wordSet.contains("camaro") || wordSet.contains("vectra") || wordSet.contains("meriva") || 
            wordSet.contains("zafira") || wordSet.contains("monza") || wordSet.contains("kadett") || 
            wordSet.contains("omega") || wordSet.contains("opala") || wordSet.contains("caravan") || 
            wordSet.contains("chevette") || wordSet.contains("sonic") || wordSet.contains("agile") || 
            wordSet.contains("captiva") || wordSet.contains("classic") || wordSet.contains("equinox") || 
            wordSet.contains("trailblazer")) {
            return "Chevrolet";
        }
        // Volkswagen
        if (wordSet.contains("gol") || wordSet.contains("golf") || wordSet.contains("polo") || 
            wordSet.contains("virtus") || wordSet.contains("nivus") || v.contains("t-cross") || 
            v.contains("t cross") || wordSet.contains("tcross") || wordSet.contains("taos") || 
            wordSet.contains("tiguan") || wordSet.contains("jetta") || wordSet.contains("passat") || 
            wordSet.contains("voyage") || wordSet.contains("saveiro") || wordSet.contains("amarok") || 
            wordSet.contains("up") || wordSet.contains("fox") || wordSet.contains("crossfox") || 
            wordSet.contains("spacefox") || wordSet.contains("bora") || wordSet.contains("santana") || 
            wordSet.contains("kombi") || wordSet.contains("fusca") || wordSet.contains("parati") || 
            wordSet.contains("quantum") || wordSet.contains("logus") || wordSet.contains("pointer") || 
            wordSet.contains("touareg")) {
            return "Volkswagen";
        }
        // Fiat
        if (wordSet.contains("uno") || wordSet.contains("mille") || wordSet.contains("palio") || 
            wordSet.contains("strada") || wordSet.contains("toro") || wordSet.contains("cronos") || 
            wordSet.contains("argo") || wordSet.contains("mobi") || wordSet.contains("pulse") || 
            wordSet.contains("fastback") || wordSet.contains("siena") || wordSet.contains("fiorino") || 
            wordSet.contains("linea") || wordSet.contains("punto") || wordSet.contains("idea") || 
            wordSet.contains("stilo") || wordSet.contains("marea") || wordSet.contains("tempra") || 
            wordSet.contains("brava") || wordSet.contains("ducato") || wordSet.contains("doblo") || 
            wordSet.contains("500")) {
            return "Fiat";
        }
        // Ford
        if (wordSet.contains("ka") || wordSet.contains("fiesta") || wordSet.contains("focus") || 
            wordSet.contains("fusion") || wordSet.contains("ecosport") || wordSet.contains("ranger") || 
            wordSet.contains("territory") || wordSet.contains("bronco") || wordSet.contains("mustang") || 
            wordSet.contains("edge") || wordSet.contains("escort") || wordSet.contains("corcel") || 
            wordSet.contains("delrey") || v.contains("del rey") || wordSet.contains("del") || 
            wordSet.contains("rey")) {
            return "Ford";
        }
        // Toyota
        if (wordSet.contains("corolla") || wordSet.contains("hilux") || wordSet.contains("yaris") || 
            wordSet.contains("etios") || wordSet.contains("sw4") || wordSet.contains("rav4") || 
            wordSet.contains("camry") || wordSet.contains("prius") || wordSet.contains("bandeirante")) {
            return "Toyota";
        }
        // Honda
        if (wordSet.contains("civic") || wordSet.contains("fit") || wordSet.contains("city") || 
            v.contains("hr-v") || v.contains("hrv") || v.contains("cr-v") || 
            v.contains("crv") || v.contains("wr-v") || v.contains("wrv") || 
            wordSet.contains("accord")) {
            return "Honda";
        }
        // Hyundai
        if (wordSet.contains("hb20") || wordSet.contains("hb20s") || wordSet.contains("creta") || 
            wordSet.contains("tucson") || wordSet.contains("ix35") || v.contains("santa fe") || 
            v.contains("santafe") || wordSet.contains("azera") || wordSet.contains("elantra") || 
            wordSet.contains("i30") || wordSet.contains("veloster") || wordSet.contains("sonata")) {
            return "Hyundai";
        }
        // Renault
        if (wordSet.contains("kwid") || wordSet.contains("sandero") || wordSet.contains("logan") || 
            wordSet.contains("duster") || wordSet.contains("oroch") || wordSet.contains("captur") || 
            wordSet.contains("kardian") || wordSet.contains("clio") || wordSet.contains("fluence") || 
            wordSet.contains("megane") || wordSet.contains("scenic")) {
            return "Renault";
        }
        // Jeep
        if (wordSet.contains("renegade") || wordSet.contains("compass") || wordSet.contains("commander") || 
            wordSet.contains("cherokee") || wordSet.contains("wrangler")) {
            return "Jeep";
        }
        // Nissan
        if (wordSet.contains("march") || wordSet.contains("versa") || wordSet.contains("kicks") || 
            wordSet.contains("sentra") || wordSet.contains("frontier") || wordSet.contains("livina") || 
            wordSet.contains("tiida")) {
            return "Nissan";
        }
        // Peugeot
        if (wordSet.contains("206") || wordSet.contains("207") || wordSet.contains("208") || 
            wordSet.contains("307") || wordSet.contains("308") || wordSet.contains("2008") || 
            wordSet.contains("3008") || wordSet.contains("408")) {
            return "Peugeot";
        }
        // Citroen
        if (wordSet.contains("c3") || wordSet.contains("c4") || wordSet.contains("c5") || 
            wordSet.contains("xsara") || wordSet.contains("aircross")) {
            return "Citroën";
        }
        // Mitsubishi
        if (wordSet.contains("l200") || wordSet.contains("asx") || wordSet.contains("outlander") || 
            wordSet.contains("pajero") || wordSet.contains("lancer")) {
            return "Mitsubishi";
        }
        // Chery
        if (wordSet.contains("tiggo") || wordSet.contains("arrizo") || wordSet.contains("qq")) {
            return "Caoa Chery";
        }
        // Kia
        if (wordSet.contains("sportage") || wordSet.contains("cerato") || wordSet.contains("picanto") || 
            wordSet.contains("soul") || wordSet.contains("sorento")) {
            return "Kia";
        }
        // BMW
        if (wordSet.contains("320i") || wordSet.contains("x1") || wordSet.contains("x3") || 
            wordSet.contains("x5") || wordSet.contains("m3")) {
            return "BMW";
        }
        // Audi
        if (wordSet.contains("a3") || wordSet.contains("a4") || wordSet.contains("a5") || 
            wordSet.contains("q3") || wordSet.contains("q5")) {
            return "Audi";
        }

        // fallback matching if string has spaces or contains brand
        String[] brands = {
            "Chevrolet", "Volkswagen", "Fiat", "Ford", "Toyota", "Honda", "Hyundai", "Renault",
            "Jeep", "Nissan", "Peugeot", "Citroën", "Citroen", "Mitsubishi", "Caoa Chery", "Chery", "Kia", "BMW",
            "Mercedes-Benz", "Mercedes", "Audi", "Volvo", "Land Rover", "Porsche", "Suzuki", "Subaru", "Ferrari", "Jac"
        };
        for (String brand : brands) {
            if (v.contains(brand.toLowerCase())) {
                if (brand.equalsIgnoreCase("citroen")) return "Citroën";
                if (brand.equalsIgnoreCase("chery")) return "Caoa Chery";
                if (brand.equalsIgnoreCase("mercedes")) return "Mercedes-Benz";
                return brand;
            }
        }

        return "Não informado";
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            if (!cellStr(row, c).isBlank()) return false;
        }
        return true;
    }

    private String limpar(String s) { return s == null ? "" : s.trim(); }

    private int parseAno(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s.replaceAll("[^0-9]", "").substring(0, 4)); }
        catch (Exception e) { return 0; }
    }

    private int parseKm(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 0; }
    }

    private BigDecimal parseValor(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        try {
            String clean = s.replaceAll("[^0-9.,]", "");
            if (clean.contains(",")) clean = clean.replace(".", "").replace(",", ".");
            return new BigDecimal(clean);
        } catch (Exception e) { return BigDecimal.ZERO; }
    }

    @GetMapping("/modelo")
    public void downloadModelo(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=modelo_importacao.xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Veiculos");
            Row header = sheet.createRow(0);
            String[] cols = {"placa","marca","modelo","ano","cor","quilometragem","valor"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            Row ex = sheet.createRow(1);
            ex.createCell(0).setCellValue("ABC1D23");
            ex.createCell(1).setCellValue("Volkswagen");
            ex.createCell(2).setCellValue("Gol 1.0");
            ex.createCell(3).setCellValue(2020);
            ex.createCell(4).setCellValue("Branco");
            ex.createCell(5).setCellValue(45000);
            ex.createCell(6).setCellValue(38500.00);
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            wb.write(response.getOutputStream());
        }
    }

    // ─── DTO interno ──────────────────────────────────────────────────────────

    public static class VeiculoImportado {
        public String placa = "", marca = "", modelo = "", ano = "",
                      cor = "", quilometragem = "", valor = "", situacao = "";
        public boolean faltaPlaca, faltaMarca, faltaModelo, faltaAno,
                       faltaCor, faltaQuilometragem, faltaValor;
        public boolean temVendido = false;

        public String detectarStatus() {
            if (temVendido) return "VENDIDO";
            return "LOJA_1";
        }
    }
}
