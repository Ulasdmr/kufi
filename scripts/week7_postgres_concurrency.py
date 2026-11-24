#!/usr/bin/env python3
"""Real simultaneous HTTP requests to a LOCAL Kufi instance backed by PostgreSQL.
No JWTs/passwords printed. Use AFTER confirming Spring runs against PostgreSQL, not H2.
"""
import argparse, datetime as dt, secrets, sys, threading
from concurrent.futures import ThreadPoolExecutor
from week7_integration_smoke import request


def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--base-url', default='http://localhost:8080')
    args=ap.parse_args()
    base=args.base_url.rstrip('/')
    if not (base.startswith('http://localhost:') or base.startswith('http://127.0.0.1:')):
        ap.error('Only localhost is supported')
    suffix=secrets.token_hex(6)
    pw='Local_Test_'+secrets.token_urlsafe(16)+'!'
    def created(status, expect, where):
        if status!=expect: raise AssertionError(f'{where} expected {expect}, received {status}')
    try:
        tokens=[]
        for role in ['BARBER','CUSTOMER','CUSTOMER']:
            status,body=request(base,'POST','/api/v1/auth/register',{
                'email':f'concurrency_{role.lower()}_{len(tokens)}_{suffix}@example.invalid',
                'password':pw,'phone':'5550001122','role':role})
            created(status,201,'registration')
            tokens.append(body['accessToken'])
        status,shop=request(base,'POST','/api/v1/shops',{
            'name':'Concurrency Test '+suffix,'city':'Istanbul','district':'Gungoren',
            'address':'Synthetic Test Address','openingTime':'09:00','closingTime':'18:00'},tokens[0])
        created(status,201,'create shop')
        status,offer=request(base,'POST',f"/api/v1/shops/{shop['shopId']}/services",{
            'name':'Test haircut','durationMinutes':45,'price':350.00},tokens[0])
        created(status,201,'create service')
        when=dt.datetime.combine((dt.datetime.now()+dt.timedelta(days=8)).date(),dt.time(14,0)).isoformat()
        body={'shopId':shop['shopId'],'serviceId':offer['serviceId'],'startTime':when}
        gate=threading.Barrier(2)
        def book(token):
            gate.wait(timeout=10)
            status,_=request(base,'POST','/api/v1/appointments',body,token)
            return status
        with ThreadPoolExecutor(max_workers=2) as pool:
            futures=[pool.submit(book,tokens[i]) for i in [1,2]]
            statuses=[f.result(timeout=30) for f in futures]
        counts=[]
        for token in tokens[1:]:
            st,entries=request(base,'GET','/api/v1/appointments/my',None,token)
            created(st,200,'retrieve customer bookings')
            counts.append(sum(a.get('shopId')==shop['shopId'] and a.get('status')=='CONFIRMED' for a in entries))
        ok=sorted(statuses)==[201,409] and sum(counts)==1
        print('Concurrent POST HTTP statuses:',sorted(statuses))
        print('Visible active appointment count across two customers:',sum(counts))
        print('R07 result:','PASS' if ok else 'FAIL')
        return 0 if ok else 1
    except Exception as ex:
        print('R07 ERROR:',str(ex)[:250],file=sys.stderr)
        return 1

if __name__=='__main__':
    sys.exit(main())
