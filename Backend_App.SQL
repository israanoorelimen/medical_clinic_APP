-- 1. Creation des tables (les meres en premier)

-- Table Patient (table mere, pas de FK)
create table patient (
   num_patient    number,
   nom            varchar2(50) not null,
   prenom         varchar2(50) not null,
   date_naissance date,
   telephone      varchar2(15),
   adresse        varchar2(100),
   constraint pk_patient primary key ( num_patient )
);

-- Table Medecin (table mere, pas de FK)
create table medecin (
   num_medecin number,
   nom         varchar2(50) not null,
   prenom      varchar2(50) not null,
   specialite  varchar2(50),
   telephone   varchar2(15),
   constraint pk_medecin primary key ( num_medecin )
);

-- Table RendezVous (table fille, elle reference Patient et Medecin)
-- Si on supprime un patient  --> ses rendez-vous sont supprimes aussi (CASCADE)
-- Si on supprime un medecin  --> ses rendez-vous sont supprimes aussi (CASCADE)
create table rendezvous (
   num_rendezvous number,
   num_patient    number,
   num_medecin    number,
   date_rdv       date,
   heure_rdv      varchar2(10),
   statut         varchar2(20) default 'Planifie',
   constraint pk_rendezvous primary key ( num_rendezvous ),
   constraint fk_rdv_patient foreign key ( num_patient )
      references patient ( num_patient )
-- Si on supprime un patient  --> ses rendez-vous sont supprimes aussi (CASCADE)
         on delete cascade,
   constraint fk_rdv_medecin foreign key ( num_medecin )
      references medecin ( num_medecin )
         on delete cascade
);


-- 2. Insertion des donnees de test

-- patients
insert into patient values ( 1,
                             'Farci',
                             'Amina',
                             to_date('1996-09-25','YYYY-MM-DD'),
                             '0523151687',
                             'Rouiba' );
insert into patient values ( 2,
                             'Kourdi',
                             'Ahmed',
                             to_date('1988-04-20','YYYY-MM-DD'),
                             '0799500976',
                             'La perouse' );
insert into patient values ( 3,
                             'Ait yahyia',
                             'Naziha',
                             to_date('1979-10-11','YYYY-MM-DD'),
                             '065743124',
                             'Hussein Dey' );

-- medecinsUPDATE patient

insert into medecin values ( 1,
                             'Triaki',
                             'Hiba',
                             'Cardiologue',
                             '0555636781' );
insert into medecin values ( 2,
                             'Boulmaali',
                             'Asmaa',
                             'Dermatologue',
                             '0555000222' );
insert into medecin values ( 3,
                             'Aissani',
                             'Rachid',
                             'OARL',
                             '0200033391' );

-- rendezvous (APRÈS)
insert into rendezvous values ( 1,
                                1,
                                1,
                                to_date('2026-05-01','YYYY-MM-DD'),
                                '08:00',
                                'Planifie' );
insert into rendezvous values ( 2,
                                2,
                                2,
                                to_date('2026-05-01','YYYY-MM-DD'),
                                '09:30',
                                'Planifie' );
insert into rendezvous values ( 3,
                                3,
                                1,
                                to_date('2026-05-01','YYYY-MM-DD'),
                                '13:00',
                                'Annule' );
commit;



-- Voir tous les patients avec la Requetes SELECT
select *
  from patient;

-- Chercher un patient par son nom
select *
  from patient
 where nom = 'Farci';

-- Voir tous les heures des rendezvous
select heure_rdv
  from rendezvous;

-- Chercher un medecin  par son telephone
select *
  from medecin
 where telephone = '0555636781';

-- Voir tous les rendez-vous avec les noms (jointure 3 tables)
select r.num_rendezvous,
       p.nom,
       p.prenom,
       m.nom,
       m.specialite,
       r.date_rdv,
       r.heure_rdv,
       r.statut
  from rendezvous r,
       patient p,
       medecin m
 where r.num_patient = p.num_patient
   and r.num_medecin = m.num_medecin;

-- Filtrer les rendez-vous par l'heure
select *
  from rendezvous
 where heure_rdv = '09:30';

-- Filtrer les rendez-vous par date
select *
  from rendezvous
 where date_rdv = to_date('2026-05-05','YYYY-MM-DD');

-- Verifier si un medecin est libre ou pa
select *
  from rendezvous
 where num_medecin = 2
   and date_rdv = to_date('2026-05-01','YYYY-MM-DD')
   and heure_rdv = '09:30'
   and statut != 'Annule';
 /*Si la resultat est une ligne → le médecin est occupé 
   Si est rien(vide) → le médecin est libre */


-- 4. Modification et suppression
--Modifier le telephone d'un patient
update patient
   set
   telephone = '0543342313'
 where num_patient = 1;

-- Modifier les infos d'un medecin
update medecin
   set
   specialite = 'Generaliste'
 where num_medecin = 1;

-- Annuler un rendez-vous (changer le statut)
update rendezvous
   set
   statut = 'Annule'
 where num_rendezvous = 1;

-- Supprimer un rendez-vous
delete from rendezvous
 where num_rendezvous = 1;

-- Supprimer un patient (ses RDV seront supprimes automatiquement grace au CASCADE)
delete from patient
 where num_patient = 3;

-- Supprimer un medecin (ses RDV seront supprimes automatiquement grace au CASCADE)
delete from medecin
 where num_medecin = 2;

commit;