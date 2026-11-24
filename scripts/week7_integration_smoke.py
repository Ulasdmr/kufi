#!/usr/bin/env python3
"""Week 7 local HTTP integration check for Kufi; Python 3 stdlib only.
Use against your own LOCAL instance only. No tokens/passwords are printed.
Run: python3 scripts/week7_integration_smoke.py --base-url http://localhost:8080
"""
import argparse, datetime as dt, json, secrets, sys, urllib.request, urllib.error


def request(base, method, path, payload=None, token=None):
    data = None if payload is None else json.dumps(payload).encode('utf-8')
    headers = {'Accept': 'application/json'}
    if data is not None:
        headers['Content-Type'] = 'application/json'
    if token:
        headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(base + path, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=15) as result:
            body = result.read().decode('utf-8')
            return result.status, json.loads(body) if body else None
    except urllib.error.HTTPError as exc:
        text = exc.read().decode('utf-8')
        try: body = json.loads(text)
        except ValueError: body = None
        return exc.code, body


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--base-url', default='http://localhost:8080', help='Local Kufi API address')
    args = parser.parse_args()
    base = args.base_url.rstrip('/')
    if not (base.startswith('http://localhost:') or base.startswith('http://127.0.0.1:')):
        parser.error('Safety: run this test against a localhost URL only')
    suffix = secrets.token_hex(5)
    password = 'Week7_' + secrets.token_urlsafe(12) + '!'
    results = []
    def check(testid, method, path, payload, token, expected):
        actual, body = request(base, method, path, payload, token)
        state = 'PASS' if actual == expected else 'FAIL'
        results.append({'id':testid, 'expected_http':expected, 'actual_http':actual, 'status':state})
        print(f'{state} {testid} HTTP expected={expected}, actual={actual}')
        if actual != expected:
            # Never print request body or auth tokens; preserve only safe error summary.
            message = body.get('message') if isinstance(body,dict) else None
            if message: print('  Error summary:',str(message)[:180])
            raise AssertionError(f'{testid} expected {expected}, received {actual}')
        return body
    try:
        check('W7-01', 'GET','/api/v1/shops',None,None,200)
        def register(role, label):
            body = check(label,'POST','/api/v1/auth/register', {
                'email':f'week7_{label.lower()}_{suffix}@example.invalid',
                'password':password,'phone':'5550001111','role':role},None,201)
            return body['accessToken']
        barber = register('BARBER','W7-02')
        client1 = register('CUSTOMER','W7-03')
        client2 = register('CUSTOMER','W7-04')
        shop = check('W7-05','POST','/api/v1/shops',{
            'name':'Week7 Integration ' + suffix,'city':'Istanbul','district':'Gungoren',
            'address':'Local Test Street','openingTime':'09:00','closingTime':'18:00'
        }, barber,201)
        shop_id = shop['shopId']
        offer = check('W7-06','POST',f'/api/v1/shops/{shop_id}/services',{
            'name':'Haircut','durationMinutes':45,'price':350.00},barber,201)
        service_id=offer['serviceId']
        check('W7-07','GET','/api/v1/shops?district=Gungoren',None,None,200)
        check('W7-08','GET',f'/api/v1/shops/{shop_id}/services',None,None,200)
        date=(dt.datetime.now()+dt.timedelta(days=4)).date()
        start1=dt.datetime.combine(date,dt.time(14,0)).isoformat(timespec='seconds')
        start2=dt.datetime.combine(date,dt.time(14,30)).isoformat(timespec='seconds')
        payload1={'shopId':shop_id,'serviceId':service_id,'startTime':start1}
        payload2={'shopId':shop_id,'serviceId':service_id,'startTime':start2}
        first=check('W7-09','POST','/api/v1/appointments',payload1,client1,201)
        check('W7-10','POST','/api/v1/appointments',payload2,client2,409)
        ident=first['appointmentId']
        check('W7-11','PATCH',f'/api/v1/appointments/{ident}/cancel',None,client2,403)
        check('W7-12','PATCH',f'/api/v1/appointments/{ident}/cancel',None,client1,200)
        check('W7-13','POST','/api/v1/appointments',payload1,client2,201)
        check('W7-14','GET','/api/v1/appointments/my',None,client2,200)
    except Exception as exc:
        print('Integration test interrupted:',str(exc)[:200],file=sys.stderr)
        print('SUMMARY:',sum(x['status']=='PASS' for x in results),'PASS,',sum(x['status']=='FAIL' for x in results),'FAIL')
        return 1
    print('SUMMARY: 14 PASS, 0 FAIL. This checks local REST + configured database, not a clean clone.')
    return 0

if __name__ == '__main__':
    sys.exit(main())
