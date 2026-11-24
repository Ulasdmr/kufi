# Kufi GitHub Week6 -> Week7 (Türkçe)

**Not:** Bu paket GitHub'a otomatik yüklenmedi. Kendi `~/Desktop/kufi/kufi` dizinindeki gerçek `.git` geçmişini koru. Çalışma ağacın temiz değilse önce mevcut değişiklikleri gör; yeni paketin tümünü kontrolsüzce kopyalama.

## 1. Kontrol ve güvenlik
```bash
cd ~/Desktop/kufi/kufi
git status -sb
git branch --show-current
git remote -v
```
Önce `week6-testing` dalında ol. Yama zaten uygulandıysa tekrar `git apply` yapma. `grep -n 'getOwner().getEmail()' src/main/java/com/kufi/backend/shop/ShopController.java` ile denetle.

## 2. Yeni paketten yalnızca Week6 belgeleri ve yardımcı scriptleri al
Paketin `~/Downloads/Kufi_Week67_GitHub_Ready/` altında açıldığını varsayıyoruz. Başka yerdeyse yolu düzelt:
```bash
rsync -avn ~/Downloads/Kufi_Week67_GitHub_Ready/docs/week-06/ docs/week-06/
rsync -av ~/Downloads/Kufi_Week67_GitHub_Ready/docs/week-06/ docs/week-06/
mkdir -p scripts
cp ~/Downloads/Kufi_Week67_GitHub_Ready/scripts/collect_week6_evidence.sh scripts/
```
Bu işlem kaynak kodunu, README'yi ve pom.xml dosyanı ezmez.

## 3. Week6 gerçek kanıt ve push
```bash
mvn clean test
bash scripts/collect_week6_evidence.sh
git diff --check
git add src/main/java src/test/java docs/week-06 evidence/week-06 scripts/collect_week6_evidence.sh pom.xml .github README.md .gitignore .env.example docker-compose.yml postman week6-pure-java
git diff --cached --check
git diff --cached --stat
git commit -m 'test: validate Kufi and fix Week 6 JPA entity access'
git push -u origin week6-testing
git rev-parse HEAD
```
`git add` belirtilen dosyalardan birini bulamazsa olmayan path'i listeden çıkar. Gizli `.env` dosyasını asla ekleme. GitHub Actions sonucunu GitHub'da doğrula; `main` için gerekirse PR aç ve CI'dan sonra merge et.

## 4. Week7 branch'i ve belgeler
```bash
git switch -c week7-integration
mkdir -p docs/week-07 scripts
rsync -avn ~/Downloads/Kufi_Week67_GitHub_Ready/docs/week-07/ docs/week-07/
rsync -av ~/Downloads/Kufi_Week67_GitHub_Ready/docs/week-07/ docs/week-07/
rsync -av ~/Downloads/Kufi_Week67_GitHub_Ready/scripts/ scripts/
```
Mevcut README dosyana hazırladığımız Week7 bölümünü ekle; GitHub workflow trigger'ına `week7-integration` dalını ekle; yeni paket içindeki README ve workflow dosyaları hazır örnektir. Kaynak kodunu ikinci kez kopyalama.

## 5. Week7 kurulum ve test
`docs/week-07/INSTALLATION_GUIDE.md` içindeki temiz clone, Docker Compose, `mvn spring-boot:run` ve `python3 scripts/week7_integration_smoke.py` işlemlerini tamamla. Gerçek çıktıyla R01-R07'yi doldur.

## 6. Week7 commit ve push
```bash
git add docs/week-07 scripts README.md .github/workflows
 git diff --cached --check
 git diff --cached --stat
 git commit -m 'docs: add Week 7 integration and reproducibility evidence'
 git push -u origin week7-integration
 git rev-parse HEAD
```
Daha sonra yeni gerçek kanıtları ayrı bir commit olarak ekleyebilirsin. Week7 raporundaki PENDING alanlarını gerçek sonuçlar olmadan PASS yapma.
