"""
GRAC(게임물관리위원회) 등급분류번호 조회 스크립트 (노래방 번호검색 repo)

⚠️ 노래방 번호검색은 **비게임** 앱이라 GRAC 대상이 아니다 (docs/grac/README.md).
이 스크립트는 앱이 '게임' 유형으로 전환될 경우(또는 추후 게임 앱)를 대비한
재조회 도구다. 게임물명으로 등급분류번호를 두 곳에서 함께 조회한다.

  1) 자체등급분류 게임물 조회   https://www.grac.or.kr/Statistics/SelfRateGameStatistics.aspx
  2) 등급분류결정 확인          https://www.grac.or.kr/Statistics/GameStatistics.aspx

사용법:
  python3 query-grac.py "리듬게임명"
  python3 query-grac.py "오목" "2024-01-01" "2026-12-31"

주의:
  - 조회 결과가 0건이면 아직 GRAC에 등록되지 않은 상태입니다.
  - 등급분류번호 포맷 예: GC-CC-NP-YYMMDD-###, SC-OM-YYMMDD-###, CC-NP-YYMMDD-###
"""
import re
import sys
import html
import http.cookiejar
import urllib.parse
import urllib.request

USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
SELF_RATE_URL = "https://www.grac.or.kr/Statistics/SelfRateGameStatistics.aspx"
RATED_URL = "https://www.grac.or.kr/Statistics/GameStatistics.aspx"


def make_opener():
    jar = http.cookiejar.CookieJar()
    return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))


def fetch(opener, url, referer, data=None):
    headers = {"User-Agent": USER_AGENT, "Referer": referer}
    req = urllib.request.Request(url, data=data, headers=headers) if data is not None else urllib.request.Request(url, headers=headers)
    return opener.open(req, timeout=40).read().decode("utf-8", "replace")


def viewstate(page_html):
    m = re.search(r'<input type="hidden" name="__VIEWSTATE" id="__VIEWSTATE" value="([^"]*)"', page_html)
    return m.group(1) if m else ""


def postback(fields):
    return "&".join(urllib.parse.quote(k, safe="") + "=" + urllib.parse.quote(v, safe="") for k, v in fields).encode("utf-8")


def data_rows(page_html):
    rows = []
    for m in re.finditer(r"<tr[^>]*>(.*?)</tr>", page_html, re.S):
        cells = [
            re.sub(r"\s+", " ", html.unescape(re.sub(r"<[^>]+>", " ", c))).strip()
            for c in re.findall(r"<td[^>]*>(.*?)</td>", m.group(1), re.S)
        ]
        joined = " ".join(cells)
        if (
            len(cells) >= 6
            and cells[0]
            and "등급분류번호" not in joined
            and "검색된 내용이 없습니다" not in joined
            and "function" not in cells[0]
        ):
            rows.append(cells)
    return rows


def search_self_rate(opener, title, date_from="", date_to=""):
    page = fetch(opener, SELF_RATE_URL, SELF_RATE_URL)
    fields = [
        ("__EVENTTARGET", "ctl00$ContentHolder$lbtnSearch"),
        ("__EVENTARGUMENT", ""),
        ("__VIEWSTATE", viewstate(page)),
        ("ctl00$ContentHolder$tbGameTitle", title),
        ("ctl00$ContentHolder$ddlGrade", ""),
        ("ctl00$ContentHolder$tbRatingNbr", ""),
        ("ctl00$ContentHolder$CalendarPicker$txtCalStartDate", date_from),
        ("ctl00$ContentHolder$CalendarPicker$txtCalEndDate", date_to),
    ]
    resp = fetch(opener, SELF_RATE_URL, SELF_RATE_URL, postback(fields))
    return data_rows(resp)


def search_rated(opener, title):
    page = fetch(opener, RATED_URL, RATED_URL)
    fields = [
        ("__EVENTTARGET", "ctl00$ContentHolder$lbtnSearch"),
        ("__EVENTARGUMENT", ""),
        ("__VIEWSTATE", viewstate(page)),
        ("ctl00$ContentHolder$txtGameTitle", title),
        ("ctl00$ContentHolder$ddlOrganization", ""),
        ("ctl00$ContentHolder$ddlApplicationGroup", ""),
        ("ctl00$ContentHolder$ddlPlatForm", ""),
        ("ctl00$ContentHolder$ddlRate", ""),
        ("ctl00$ContentHolder$txtRatingNbr", ""),
        ("ctl00$ContentHolder$txtEntName", ""),
        ("ctl00$ContentHolder$txtStartDate", ""),
        ("ctl00$ContentHolder$txtEndDate", ""),
    ]
    resp = fetch(opener, RATED_URL, RATED_URL, postback(fields))
    return data_rows(resp)


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    title = sys.argv[1]
    date_from = sys.argv[2] if len(sys.argv) > 2 else ""
    date_to = sys.argv[3] if len(sys.argv) > 3 else ""
    opener = make_opener()

    print(f"== 자체등급분류 게임물 조회: {title!r} ==")
    rows = search_self_rate(opener, title, date_from, date_to)
    if not rows:
        print("  (0건 — 아직 GRAC 자체등급분류 DB에 없는 게임)")
    for r in rows:
        print("  ", r[:6])

    print(f"== 등급분류결정 확인: {title!r} ==")
    rows = search_rated(opener, title)
    if not rows:
        print("  (0건 — 등급분류 결정 내역 없음)")
    for r in rows:
        print("  ", r[:6])


if __name__ == "__main__":
    main()